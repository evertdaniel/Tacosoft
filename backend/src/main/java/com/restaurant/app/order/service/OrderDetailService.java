package com.restaurant.app.order.service;

import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.model.Product;
import com.restaurant.app.menu.model.ProductOption;
import com.restaurant.app.menu.repository.ProductOptionRepository;
import com.restaurant.app.menu.repository.ProductRepository;
import com.restaurant.app.menu.service.ProductService;
import com.restaurant.app.order.dto.OrderDetailDto;
import com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest;
import com.restaurant.app.order.mapper.OrderMapper;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.model.OrderDetail;
import com.restaurant.app.order.repository.OrderDetailRepository;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for order detail operations with status transitions and total calculation (INV-04). */
@Service
public class OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // Valid status transitions for order details
    private static final Set<String> PENDING_TRANSITIONS = Set.of("IN_PROGRESS", "CANCELLED");
    private static final Set<String> IN_PROGRESS_TRANSITIONS = Set.of("READY", "CANCELLED");
    private static final Set<String> READY_TRANSITIONS = Set.of("DELIVERED");
    private static final Set<String> DELIVERED_TRANSITIONS = Set.of(); // Terminal state
    private static final Set<String> CANCELLED_TRANSITIONS = Set.of(); // Terminal state

    public OrderDetailService(
            OrderDetailRepository orderDetailRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            ProductOptionRepository productOptionRepository,
            ProductService productService,
            OrderMapper orderMapper,
            SimpMessagingTemplate messagingTemplate) {
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productOptionRepository = productOptionRepository;
        this.productService = productService;
        this.orderMapper = orderMapper;
        this.messagingTemplate = messagingTemplate;
    }

    /** Get order detail by ID. */
    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetailById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        OrderDetail detail =
                orderDetailRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("OrderDetail", id));

        // Fetch related entities for DTO
        Product product =
                detail.getProduct() != null
                        ? detail.getProduct()
                        : productRepository.findById(detail.getProductId()).orElse(null);

        ProductOption option =
                detail.getProductOptionId() != null
                        ? (detail.getProductOption() != null
                                ? detail.getProductOption()
                                : productOptionRepository
                                        .findById(detail.getProductOptionId())
                                        .orElse(null))
                        : null;

        // Update detail with fetched entities
        detail.setProduct(product);
        detail.setProductOption(option);

        return orderMapper.toDetailDto(detail);
    }

    /** Get details by order ID. */
    @Transactional(readOnly = true)
    public java.util.List<OrderDetailDto> getDetailsByOrderId(String orderId) {
        String restaurantId = TenantContext.getRestaurantId();
        return orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, orderId).stream()
                .map(
                        detail -> {
                            // Fetch related entities
                            Product product =
                                    productRepository.findById(detail.getProductId()).orElse(null);
                            ProductOption option =
                                    detail.getProductOptionId() != null
                                            ? productOptionRepository
                                                    .findById(detail.getProductOptionId())
                                                    .orElse(null)
                                            : null;
                            detail.setProduct(product);
                            detail.setProductOption(option);
                            return orderMapper.toDetailDto(detail);
                        })
                .toList();
    }

    /** Update order detail status with transition validation. */
    @Transactional
    public OrderDetailDto updateOrderDetailStatus(
            String detailId, UpdateOrderDetailStatusRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        OrderDetail detail =
                orderDetailRepository
                        .findByIdAndRestaurantId(detailId, restaurantId)
                        .orElseThrow(() -> new NotFoundException("OrderDetail", detailId));

        String currentStatus = detail.getStatus();
        String newStatus = request.getStatus();

        // Validate status transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new ConflictException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        detail.setStatus(newStatus);

        // If cancelled, restore stock
        if ("CANCELLED".equals(newStatus) && !"CANCELLED".equals(currentStatus)) {
            productService.updateStock(detail.getProductId(), detail.getQuantity());
        }

        detail = orderDetailRepository.save(detail);

        // Recalculate order total (INV-04)
        recalculateOrderTotal(detail.getOrderId());

        // Derive order status from detail statuses (SPEC-ORDER-002)
        deriveAndUpdateOrderStatus(detail.getOrderId());

        // Broadcast detail status change
        broadcastOrderDetailChange(detail);

        return orderMapper.toDetailDto(detail);
    }

    /** Create order detail (used by OrderService). */
    @Transactional
    public OrderDetail createOrderDetail(
            Order order, com.restaurant.app.order.dto.CreateOrderDetailRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        // Validate product exists and is available
        Product product =
                productRepository
                        .findByIdAndRestaurantId(request.getProductId(), restaurantId)
                        .orElseThrow(
                                () -> new NotFoundException("Product", request.getProductId()));

        if (!"AVAILABLE".equals(product.getStatus())) {
            throw new ConflictException("Product is not available: " + product.getName());
        }

        // Validate product option if provided
        ProductOption productOption = null;
        if (request.getProductOptionId() != null) {
            productOption =
                    productOptionRepository
                            .findByIdAndRestaurantId(request.getProductOptionId(), restaurantId)
                            .orElseThrow(
                                    () ->
                                            new NotFoundException(
                                                    "ProductOption", request.getProductOptionId()));

            if (!productOption.isAvailable()) {
                throw new ConflictException(
                        "Product option is not available: " + productOption.getName());
            }
        }

        // Calculate pricing
        BigDecimal unitPrice = product.getPrice();
        if (productOption != null) {
            unitPrice = unitPrice.add(productOption.getPriceAdjustment());
        }

        BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        // Create order detail
        OrderDetail detail =
                OrderDetail.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .orderId(order.getId())
                        .productId(request.getProductId())
                        .quantity(request.getQuantity())
                        .price(unitPrice)
                        .amount(amount)
                        .status("PENDING")
                        .notes(request.getNotes())
                        .productOptionId(request.getProductOptionId())
                        .build();

        detail = orderDetailRepository.save(detail);

        // Update stock
        productService.updateStock(request.getProductId(), -request.getQuantity());

        return detail;
    }

    /** Validate if a status transition is allowed. */
    private boolean isValidTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return true; // No change
        }

        return switch (currentStatus) {
            case "PENDING" -> PENDING_TRANSITIONS.contains(newStatus);
            case "IN_PROGRESS" -> IN_PROGRESS_TRANSITIONS.contains(newStatus);
            case "READY" -> READY_TRANSITIONS.contains(newStatus);
            case "DELIVERED" -> DELIVERED_TRANSITIONS.contains(newStatus);
            case "CANCELLED" -> CANCELLED_TRANSITIONS.contains(newStatus);
            default -> false;
        };
    }

    /** Recalculate order total (INV-04 invariant). order.total = Σ(order_detail.amount) */
    private void recalculateOrderTotal(String orderId) {
        String restaurantId = TenantContext.getRestaurantId();
        Order order =
                orderRepository
                        .findByIdAndRestaurantId(orderId, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Order", orderId));

        BigDecimal newTotal =
                orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, orderId).stream()
                        .filter(detail -> !"CANCELLED".equals(detail.getStatus()))
                        .map(OrderDetail::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotal(newTotal);
        orderRepository.save(order);

        // Broadcast order total change
        broadcastOrderChange(order);
    }

    /**
     * Derive order status from its detail statuses (SPEC-ORDER-002).
     *
     * <p>Rules: - All non-cancelled details DELIVERED → order DELIVERED - Any detail IN_PROGRESS or
     * READY → order IN_PROGRESS - All details CANCELLED → order CANCELLED - Otherwise remain
     * PENDING
     */
    private void deriveAndUpdateOrderStatus(String orderId) {
        String restaurantId = TenantContext.getRestaurantId();
        Order order =
                orderRepository
                        .findByIdAndRestaurantId(orderId, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Order", orderId));

        List<OrderDetail> details =
                orderDetailRepository.findByRestaurantIdAndOrderId(restaurantId, orderId);

        if (details.isEmpty()) {
            return;
        }

        List<OrderDetail> activeDetails =
                details.stream().filter(d -> !"CANCELLED".equals(d.getStatus())).toList();

        String derivedStatus;
        if (activeDetails.isEmpty()) {
            derivedStatus = "CANCELLED";
        } else if (activeDetails.stream().allMatch(d -> "DELIVERED".equals(d.getStatus()))) {
            derivedStatus = "DELIVERED";
        } else if (activeDetails.stream()
                .anyMatch(
                        d ->
                                "IN_PROGRESS".equals(d.getStatus())
                                        || "READY".equals(d.getStatus()))) {
            derivedStatus = "IN_PROGRESS";
        } else {
            derivedStatus = "PENDING";
        }

        if (!derivedStatus.equals(order.getStatus())) {
            order.setStatus(derivedStatus);
            orderRepository.save(order);
            broadcastOrderChange(order);
        }
    }

    /** Broadcast order detail change to WebSocket topic. */
    private void broadcastOrderDetailChange(OrderDetail detail) {
        String restaurantId = TenantContext.getRestaurantId();

        // Fetch entities for broadcast
        Product product = productRepository.findById(detail.getProductId()).orElse(null);
        ProductOption option =
                detail.getProductOptionId() != null
                        ? productOptionRepository.findById(detail.getProductOptionId()).orElse(null)
                        : null;
        detail.setProduct(product);
        detail.setProductOption(option);

        messagingTemplate.convertAndSend(
                "/topic/restaurant/" + restaurantId + "/orders", orderMapper.toDetailDto(detail));
    }

    /** Broadcast order change to WebSocket topic. */
    private void broadcastOrderChange(Order order) {
        String restaurantId = TenantContext.getRestaurantId();
        // Full order broadcast would be done here
        // For now, we broadcast a simple update event
        messagingTemplate.convertAndSend(
                "/topic/restaurant/" + restaurantId + "/orders/" + order.getId(), "ORDER_UPDATED");
    }
}
