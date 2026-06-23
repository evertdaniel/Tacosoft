package com.restaurant.app.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.model.Product;
import com.restaurant.app.menu.model.ProductOption;
import com.restaurant.app.menu.repository.ProductOptionRepository;
import com.restaurant.app.menu.repository.ProductRepository;
import com.restaurant.app.menu.service.ProductService;
import com.restaurant.app.order.dto.CreateOrderDetailRequest;
import com.restaurant.app.order.dto.OrderDetailDto;
import com.restaurant.app.order.mapper.OrderMapper;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.model.OrderDetail;
import com.restaurant.app.order.repository.OrderDetailRepository;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/** Unit tests for OrderDetailService. SPEC-ORDER-002, INV-04 (order total calculation). */
@ExtendWith(MockitoExtension.class)
class OrderDetailServiceTest {

    @Mock private OrderDetailRepository orderDetailRepository;

    @Mock private OrderRepository orderRepository;

    @Mock private ProductRepository productRepository;

    @Mock private ProductOptionRepository productOptionRepository;

    @Mock private ProductService productService;

    @Mock private OrderMapper orderMapper;

    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private OrderDetailService orderDetailService;

    private final String restaurantId = "restaurant-1";
    private Order testOrder;

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
        testOrder = createTestOrder();

        lenient().doNothing().when(productService).updateStock(anyString(), anyInt());
        lenient()
                .when(orderMapper.toDetailDto(any(OrderDetail.class)))
                .thenAnswer(
                        invocation -> {
                            OrderDetail detail = invocation.getArgument(0);
                            OrderDetailDto dto = new OrderDetailDto();
                            dto.setId(detail.getId());
                            dto.setOrderId(detail.getOrderId());
                            dto.setProductId(detail.getProductId());
                            dto.setProductName("Test Product");
                            dto.setQuantity(detail.getQuantity());
                            dto.setUnitPrice(detail.getPrice());
                            dto.setAmount(detail.getAmount());
                            dto.setStatus(detail.getStatus());
                            return dto;
                        });
        lenient()
                .when(orderRepository.findByIdAndRestaurantId(anyString(), eq(restaurantId)))
                .thenReturn(Optional.of(testOrder));
        lenient().doNothing().when(messagingTemplate).convertAndSend(anyString(), (Object) any());
        lenient()
                .when(
                        productOptionRepository.findByIdAndRestaurantId(
                                anyString(), eq(restaurantId)))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createOrderDetail_ValidProduct_CreatesDetailSuccessfully() {
        // Arrange
        CreateOrderDetailRequest request = new CreateOrderDetailRequest();
        request.setProductId("product-1");
        request.setQuantity(2);
        request.setNotes("Special instructions");

        Product product = createTestProduct(BigDecimal.valueOf(25));
        when(productRepository.findByIdAndRestaurantId("product-1", restaurantId))
                .thenReturn(Optional.of(product));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderDetail result = orderDetailService.createOrderDetail(testOrder, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(testOrder.getId());
        assertThat(result.getProductId()).isEqualTo("product-1");
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getPrice()).isEqualTo(product.getPrice());
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(50)); // 2 * 25
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getNotes()).isEqualTo("Special instructions");
    }

    @Test
    void createOrderDetail_ProductNotFound_ThrowsNotFoundException() {
        // Arrange
        CreateOrderDetailRequest request = new CreateOrderDetailRequest();
        request.setProductId("nonexistent");
        request.setQuantity(1);

        when(productRepository.findByIdAndRestaurantId("nonexistent", restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderDetailService.createOrderDetail(testOrder, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createOrderDetail_CalculatesAmountCorrectly() {
        // Arrange
        CreateOrderDetailRequest request = new CreateOrderDetailRequest();
        request.setProductId("product-1");
        request.setQuantity(3);

        Product product = createTestProduct(BigDecimal.valueOf(15.50));
        when(productRepository.findByIdAndRestaurantId("product-1", restaurantId))
                .thenReturn(Optional.of(product));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderDetail result = orderDetailService.createOrderDetail(testOrder, request);

        // Assert - amount = quantity * unitPrice
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(46.50)); // 3 * 15.50
    }

    @Test
    void updateStatus_ValidTransition_UpdatesStatus() {
        // Arrange
        OrderDetail detail = createOrderDetail("PENDING");
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("IN_PROGRESS");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderDetailDto result = orderDetailService.updateOrderDetailStatus("detail-1", request);

        // Assert
        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void updateStatus_InvalidTransition_ThrowsConflictException() {
        // Arrange
        OrderDetail detail = createOrderDetail("PENDING");
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("DELIVERED");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));

        // Act & Assert - Cannot jump from PENDING to DELIVERED
        assertThatThrownBy(() -> orderDetailService.updateOrderDetailStatus("detail-1", request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_CanTransitionThroughValidStates() {
        // Arrange
        OrderDetail detail = createOrderDetail("PENDING");
        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request1 =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request1.setStatus("IN_PROGRESS");

        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request2 =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request2.setStatus("READY");

        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request3 =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request3.setStatus("DELIVERED");

        // Act - Valid progression: PENDING -> IN_PROGRESS -> READY -> DELIVERED
        OrderDetailDto result1 = orderDetailService.updateOrderDetailStatus("detail-1", request1);
        assertThat(result1.getStatus()).isEqualTo("IN_PROGRESS");

        detail.setStatus("IN_PROGRESS");
        OrderDetailDto result2 = orderDetailService.updateOrderDetailStatus("detail-1", request2);
        assertThat(result2.getStatus()).isEqualTo("READY");

        detail.setStatus("READY");
        OrderDetailDto result3 = orderDetailService.updateOrderDetailStatus("detail-1", request3);
        assertThat(result3.getStatus()).isEqualTo("DELIVERED");
    }

    @Test
    void updateStatus_AllowsCancellationFromPending() {
        // Arrange
        OrderDetail detail = createOrderDetail("PENDING");
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("CANCELLED");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderDetailDto result = orderDetailService.updateOrderDetailStatus("detail-1", request);

        // Assert
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void updateStatus_DetailNotFound_ThrowsNotFoundException() {
        // Arrange
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("READY");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderDetailService.updateOrderDetailStatus("detail-1", request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getDetailsByOrderId_ReturnsAllDetails() {
        // Arrange
        List<OrderDetail> details =
                List.of(createOrderDetail("PENDING"), createOrderDetail("READY"));
        when(orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, "order-1"))
                .thenReturn(details);

        // Act
        List<OrderDetailDto> result = orderDetailService.getDetailsByOrderId("order-1");

        // Assert
        assertThat(result).hasSize(2);
        verify(orderDetailRepository).findByRestaurantIdAndOrderId(restaurantId, "order-1");
    }

    @Test
    void updateStatus_OneDetailInProgress_DerivesOrderInProgress() {
        // Arrange
        OrderDetail detail = createOrderDetail("PENDING");
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("IN_PROGRESS");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, "order-1"))
                .thenReturn(List.of(detail));

        // Act
        orderDetailService.updateOrderDetailStatus("detail-1", request);

        // Assert
        assertThat(testOrder.getStatus()).isEqualTo("IN_PROGRESS");
        verify(orderRepository, times(2)).save(testOrder);
    }

    @Test
    void updateStatus_AllDetailsDelivered_DerivesOrderDelivered() {
        // Arrange
        OrderDetail detail = createOrderDetail("READY");
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("DELIVERED");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, "order-1"))
                .thenReturn(List.of(detail));

        // Act
        orderDetailService.updateOrderDetailStatus("detail-1", request);

        // Assert
        assertThat(testOrder.getStatus()).isEqualTo("DELIVERED");
        verify(orderRepository, times(2)).save(testOrder);
    }

    @Test
    void updateStatus_AllActiveDetailsCancelled_DerivesOrderCancelled() {
        // Arrange
        OrderDetail detail = createOrderDetail("PENDING");
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("CANCELLED");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, "order-1"))
                .thenReturn(List.of(detail));

        // Act
        orderDetailService.updateOrderDetailStatus("detail-1", request);

        // Assert
        assertThat(testOrder.getStatus()).isEqualTo("CANCELLED");
        verify(orderRepository, times(2)).save(testOrder);
    }

    @Test
    void updateStatus_CancelledFromPending_RestoresStock() {
        // Arrange
        OrderDetail detail = createOrderDetail("PENDING");
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("CANCELLED");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, "order-1"))
                .thenReturn(List.of(detail));

        // Act
        orderDetailService.updateOrderDetailStatus("detail-1", request);

        // Assert - Stock restored by quantity
        verify(productService).updateStock("product-1", 2);
    }

    @Test
    void updateStatus_SameStatus_AllowedWithoutSideEffects() {
        // Arrange
        OrderDetail detail = createOrderDetail("PENDING");
        com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest request =
                new com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest();
        request.setStatus("PENDING");

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, "order-1"))
                .thenReturn(List.of(detail));

        // Act
        OrderDetailDto result = orderDetailService.updateOrderDetailStatus("detail-1", request);

        // Assert
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(productService, never()).updateStock(anyString(), anyInt());
    }

    @Test
    void getOrderDetailById_ExistingDetail_ReturnsDetail() {
        // Arrange
        OrderDetail detail = createOrderDetail("READY");
        Product product = createTestProduct(BigDecimal.valueOf(25));

        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.of(detail));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        // Act
        OrderDetailDto result = orderDetailService.getOrderDetailById("detail-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("detail-1");
        assertThat(result.getStatus()).isEqualTo("READY");
    }

    @Test
    void getOrderDetailById_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(orderDetailRepository.findByIdAndRestaurantId("detail-1", restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderDetailService.getOrderDetailById("detail-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createOrderDetail_WithProductOption_CalculatesAdjustedPrice() {
        // Arrange
        CreateOrderDetailRequest request = new CreateOrderDetailRequest();
        request.setProductId("product-1");
        request.setProductOptionId("option-1");
        request.setQuantity(2);

        Product product = createTestProduct(BigDecimal.valueOf(25));
        ProductOption option =
                ProductOption.builder()
                        .id("option-1")
                        .restaurantId(restaurantId)
                        .name("Extra cheese")
                        .priceAdjustment(BigDecimal.valueOf(5))
                        .isAvailable(true)
                        .build();

        when(productRepository.findByIdAndRestaurantId("product-1", restaurantId))
                .thenReturn(Optional.of(product));
        when(productOptionRepository.findByIdAndRestaurantId("option-1", restaurantId))
                .thenReturn(Optional.of(option));
        when(orderDetailRepository.save(any(OrderDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderDetail result = orderDetailService.createOrderDetail(testOrder, request);

        // Assert - unitPrice = 25 + 5, amount = 2 * 30
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(30));
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(60));
        assertThat(result.getProductOptionId()).isEqualTo("option-1");
    }

    @Test
    void createOrderDetail_ProductOptionNotAvailable_ThrowsConflictException() {
        // Arrange
        CreateOrderDetailRequest request = new CreateOrderDetailRequest();
        request.setProductId("product-1");
        request.setProductOptionId("option-1");
        request.setQuantity(1);

        Product product = createTestProduct(BigDecimal.valueOf(25));
        ProductOption option =
                ProductOption.builder()
                        .id("option-1")
                        .restaurantId(restaurantId)
                        .name("Extra cheese")
                        .priceAdjustment(BigDecimal.valueOf(5))
                        .isAvailable(false)
                        .build();

        when(productRepository.findByIdAndRestaurantId("product-1", restaurantId))
                .thenReturn(Optional.of(product));
        when(productOptionRepository.findByIdAndRestaurantId("option-1", restaurantId))
                .thenReturn(Optional.of(option));

        // Act & Assert
        assertThatThrownBy(() -> orderDetailService.createOrderDetail(testOrder, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Product option is not available");
    }

    // Helper methods

    private Order createTestOrder() {
        return Order.builder()
                .id("order-1")
                .restaurantId(restaurantId)
                .num(1)
                .type("IN_PLACE")
                .status("PENDING")
                .total(BigDecimal.ZERO)
                .build();
    }

    private Product createTestProduct(BigDecimal price) {
        return Product.builder()
                .id("product-1")
                .restaurantId(restaurantId)
                .name("Test Product")
                .price(price)
                .taxRate(BigDecimal.valueOf(16))
                .status("AVAILABLE")
                .build();
    }

    private OrderDetail createOrderDetail(String status) {
        return OrderDetail.builder()
                .id("detail-1")
                .orderId("order-1")
                .productId("product-1")
                .quantity(2)
                .price(BigDecimal.valueOf(25))
                .amount(BigDecimal.valueOf(50))
                .status(status)
                .build();
    }

    private OrderDetailDto createOrderDetailDto() {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId("detail-1");
        dto.setOrderId("order-1");
        dto.setProductId("product-1");
        dto.setProductName("Test Product");
        dto.setQuantity(2);
        dto.setUnitPrice(BigDecimal.valueOf(25));
        dto.setAmount(BigDecimal.valueOf(50));
        dto.setStatus("PENDING");
        return dto;
    }
}
