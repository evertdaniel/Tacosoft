package com.restaurant.app.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.repository.ProductRepository;
import com.restaurant.app.order.dto.CreateOrderDetailRequest;
import com.restaurant.app.order.dto.CreateOrderRequest;
import com.restaurant.app.order.dto.OrderDto;
import com.restaurant.app.order.mapper.OrderMapper;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderDetailRepository;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import com.restaurant.app.table.model.RestaurantTable;
import com.restaurant.app.table.repository.TableRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/** Unit tests for OrderService. SPEC-ORDER-001, SPEC-ORDER-002, INV-04. */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;

    @Mock private TableRepository tableRepository;

    @Mock private ProductRepository productRepository;

    @Mock private OrderDetailService orderDetailService;

    @Mock private OrderDetailRepository orderDetailRepository;

    @Mock private OrderMapper orderMapper;

    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private OrderService orderService;

    private final String restaurantId = "restaurant-1";
    private CreateOrderRequest validRequest;

    @BeforeEach
    void setUp() {
        // Set tenant context
        TenantContext.setRestaurantId(restaurantId);

        // Create valid order request
        CreateOrderDetailRequest detail1 = new CreateOrderDetailRequest();
        detail1.setProductId("product-1");
        detail1.setQuantity(2);

        CreateOrderDetailRequest detail2 = new CreateOrderDetailRequest();
        detail2.setProductId("product-2");
        detail2.setProductOptionId("option-1");
        detail2.setQuantity(1);

        validRequest = new CreateOrderRequest();
        validRequest.setType("IN_PLACE");
        validRequest.setTableId("table-1");
        validRequest.setPeople(4);
        validRequest.setClientId("client-1");
        validRequest.setDetails(List.of(detail1, detail2));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createOrder_ValidInPlaceOrder_CreatesOrderSuccessfully() {
        // Arrange
        RestaurantTable table = createAvailableTable();
        when(tableRepository.findByIdAndRestaurantId("table-1", restaurantId))
                .thenReturn(Optional.of(table));
        when(orderRepository.findMaxNumByRestaurantId(restaurantId)).thenReturn(null);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailService.createOrderDetail(
                        any(Order.class), any(CreateOrderDetailRequest.class)))
                .thenReturn(createOrderDetail(BigDecimal.valueOf(50)));
        when(orderDetailService.getDetailsByOrderId(anyString())).thenReturn(List.of());
        when(orderMapper.toDto(any(Order.class), any())).thenReturn(createOrderDto());

        // Act
        OrderDto result = orderService.createOrder(validRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository, times(2))
                .save(argThat(order -> order.getNum() == 1 && order.getType().equals("IN_PLACE")));
        verify(tableRepository).save(argThat(t -> t.getStatus().equals("OCCUPIED")));
        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/restaurant/" + restaurantId + "/orders"), any(OrderDto.class));
    }

    @Test
    void createOrder_InPlaceOrderWithoutTable_ThrowsConflictException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("IN_PLACE");
        request.setPeople(4);
        request.setDetails(List.of());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("IN_PLACE orders must have a table");
    }

    @Test
    void createOrder_TableNotAvailable_ThrowsConflictException() {
        // Arrange
        RestaurantTable occupiedTable =
                RestaurantTable.builder()
                        .id("table-1")
                        .restaurantId(restaurantId)
                        .num(1)
                        .status("OCCUPIED")
                        .build();

        when(tableRepository.findByIdAndRestaurantId("table-1", restaurantId))
                .thenReturn(Optional.of(occupiedTable));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void createOrder_GeneratesSequentialOrderNumbers() {
        // Arrange
        when(tableRepository.findByIdAndRestaurantId("table-1", restaurantId))
                .thenReturn(Optional.of(createAvailableTable()));
        when(orderRepository.findMaxNumByRestaurantId(restaurantId)).thenReturn(99);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailService.createOrderDetail(
                        any(Order.class), any(CreateOrderDetailRequest.class)))
                .thenReturn(createOrderDetail(BigDecimal.valueOf(50)));
        when(orderDetailService.getDetailsByOrderId(anyString())).thenReturn(List.of());
        when(orderMapper.toDto(any(Order.class), any())).thenReturn(createOrderDto());

        // Act
        orderService.createOrder(validRequest);

        // Assert
        verify(orderRepository, times(2)).save(argThat(order -> order.getNum() == 100));
    }

    @Test
    void createOrder_CalculatesTotalCorrectly_INV04() {
        // Arrange - This is the INV-04 invariant test
        when(tableRepository.findByIdAndRestaurantId("table-1", restaurantId))
                .thenReturn(Optional.of(createAvailableTable()));
        when(orderRepository.findMaxNumByRestaurantId(restaurantId)).thenReturn(null);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(
                        invocation -> {
                            Order order = invocation.getArgument(0);
                            // First save sets total to 0
                            return order;
                        });
        when(orderDetailService.createOrderDetail(
                        any(Order.class), any(CreateOrderDetailRequest.class)))
                .thenReturn(createOrderDetail(BigDecimal.valueOf(50)))
                .thenReturn(createOrderDetail(BigDecimal.valueOf(30)));
        when(orderDetailService.getDetailsByOrderId(anyString())).thenReturn(List.of());
        when(orderMapper.toDto(any(Order.class), any())).thenReturn(createOrderDto());

        // Act
        orderService.createOrder(validRequest);

        // Assert - Verify order.total = Σ(order_detail.amount)
        verify(orderRepository, times(2))
                .save(argThat(order -> order.getTotal().compareTo(BigDecimal.valueOf(80)) == 0));
    }

    @Test
    void createOrder_TakeAwayType_NoTableRequired() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("TAKE_AWAY");
        request.setPeople(1);
        request.setDetails(List.of());

        when(orderRepository.findMaxNumByRestaurantId(restaurantId)).thenReturn(null);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailService.getDetailsByOrderId(anyString())).thenReturn(List.of());
        when(orderMapper.toDto(any(Order.class), any())).thenReturn(createOrderDto());

        // Act
        OrderDto result = orderService.createOrder(request);

        // Assert
        assertThat(result).isNotNull();
        verify(tableRepository, never()).save(any());
    }

    @Test
    void deleteOrder_PendingOrder_DeletesSuccessfully() {
        // Arrange
        Order pendingOrder = createPendingOrder();
        when(orderRepository.findByIdAndRestaurantId("order-1", restaurantId))
                .thenReturn(Optional.of(pendingOrder));
        when(orderDetailRepository.findByOrderId("order-1")).thenReturn(List.of());

        // Act
        orderService.deleteOrder("order-1");

        // Assert
        verify(orderRepository).delete(pendingOrder);
    }

    @Test
    void deleteOrder_NonPendingOrder_ThrowsConflictException() {
        // Arrange
        Order activeOrder = createActiveOrder();
        when(orderRepository.findByIdAndRestaurantId("order-1", restaurantId))
                .thenReturn(Optional.of(activeOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.deleteOrder("order-1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void deleteOrder_OrderNotFound_ThrowsNotFoundException() {
        // Arrange
        when(orderRepository.findByIdAndRestaurantId("order-1", restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.deleteOrder("order-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOrderById_ExistingOrder_ReturnsOrder() {
        // Arrange
        Order order = createPendingOrder();
        when(orderRepository.findByIdAndRestaurantId("order-1", restaurantId))
                .thenReturn(Optional.of(order));
        when(orderDetailService.getDetailsByOrderId("order-1")).thenReturn(List.of());
        when(orderMapper.toDto(eq(order), any())).thenReturn(createOrderDto());

        // Act
        OrderDto result = orderService.getOrderById("order-1");

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findByIdAndRestaurantId("order-1", restaurantId);
    }

    @Test
    void getActiveOrders_ReturnsOnlyActiveOrders() {
        // Arrange
        when(orderRepository.findByRestaurantIdAndStatusNotIn(
                        restaurantId, List.of("COMPLETED", "CANCELLED")))
                .thenReturn(List.of(createPendingOrder()));
        when(orderDetailService.getDetailsByOrderId(anyString())).thenReturn(List.of());
        when(orderMapper.toDto(any(Order.class), any())).thenReturn(createOrderDto());

        // Act
        List<OrderDto> result = orderService.getActiveOrders();

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository)
                .findByRestaurantIdAndStatusNotIn(restaurantId, List.of("COMPLETED", "CANCELLED"));
    }

    // Helper methods

    private RestaurantTable createAvailableTable() {
        return RestaurantTable.builder()
                .id("table-1")
                .restaurantId(restaurantId)
                .num(1)
                .status("AVAILABLE")
                .build();
    }

    private Order createPendingOrder() {
        return Order.builder()
                .id("order-1")
                .restaurantId(restaurantId)
                .num(1)
                .type("IN_PLACE")
                .status("PENDING")
                .total(BigDecimal.valueOf(100))
                .build();
    }

    private Order createActiveOrder() {
        return Order.builder()
                .id("order-1")
                .restaurantId(restaurantId)
                .num(1)
                .type("IN_PLACE")
                .status("IN_PROGRESS")
                .total(BigDecimal.valueOf(100))
                .build();
    }

    private com.restaurant.app.order.model.OrderDetail createOrderDetail(BigDecimal amount) {
        return com.restaurant.app.order.model.OrderDetail.builder()
                .id(UUID.randomUUID().toString())
                .orderId("order-1")
                .productId("product-1")
                .quantity(2)
                .price(BigDecimal.valueOf(25))
                .amount(amount)
                .status("PENDING")
                .build();
    }

    private OrderDto createOrderDto() {
        OrderDto dto = new OrderDto();
        dto.setId("order-1");
        dto.setNum(1);
        dto.setType("IN_PLACE");
        dto.setStatus("PENDING");
        dto.setTotal(BigDecimal.valueOf(100));
        dto.setPeople(4);
        dto.setTableId("table-1");
        dto.setClientId("client-1");
        dto.setDetails(List.of());
        return dto;
    }
}
