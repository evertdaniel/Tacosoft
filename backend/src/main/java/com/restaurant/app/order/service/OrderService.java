package com.restaurant.app.order.service;

import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.repository.ProductRepository;
import com.restaurant.app.order.dto.CreateOrderRequest;
import com.restaurant.app.order.dto.OrderDetailDto;
import com.restaurant.app.order.dto.OrderDto;
import com.restaurant.app.order.mapper.OrderMapper;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.model.OrderDetail;
import com.restaurant.app.order.repository.OrderDetailRepository;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import com.restaurant.app.security.UserDetailsAdapter;
import com.restaurant.app.table.model.RestaurantTable;
import com.restaurant.app.table.repository.TableRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for order operations with WebSocket broadcasting. */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;
    private final ProductRepository productRepository;
    private final OrderDetailService orderDetailService;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderMapper orderMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderService(
            OrderRepository orderRepository,
            TableRepository tableRepository,
            ProductRepository productRepository,
            OrderDetailService orderDetailService,
            OrderDetailRepository orderDetailRepository,
            OrderMapper orderMapper,
            SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.productRepository = productRepository;
        this.orderDetailService = orderDetailService;
        this.orderDetailRepository = orderDetailRepository;
        this.orderMapper = orderMapper;
        this.messagingTemplate = messagingTemplate;
    }

    /** Get all orders for current restaurant. */
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        String restaurantId = TenantContext.getRestaurantId();
        return orderRepository.findByRestaurantId(restaurantId).stream()
                .map(
                        order -> {
                            List<OrderDetailDto> details =
                                    orderDetailService.getDetailsByOrderId(order.getId());
                            return orderMapper.toDto(order, details);
                        })
                .toList();
    }

    /** Get active orders (not completed/cancelled). */
    @Transactional(readOnly = true)
    public List<OrderDto> getActiveOrders() {
        String restaurantId = TenantContext.getRestaurantId();
        return orderRepository
                .findByRestaurantIdAndStatusNotIn(restaurantId, List.of("COMPLETED", "CANCELLED"))
                .stream()
                .map(
                        order -> {
                            List<OrderDetailDto> details =
                                    orderDetailService.getDetailsByOrderId(order.getId());
                            return orderMapper.toDto(order, details);
                        })
                .toList();
    }

    /** Get orders by status. */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(String status) {
        String restaurantId = TenantContext.getRestaurantId();
        return orderRepository.findByRestaurantIdAndStatus(restaurantId, status).stream()
                .map(
                        order -> {
                            List<OrderDetailDto> details =
                                    orderDetailService.getDetailsByOrderId(order.getId());
                            return orderMapper.toDto(order, details);
                        })
                .toList();
    }

    /** Get an order by ID. */
    @Transactional(readOnly = true)
    public OrderDto getOrderById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Order order =
                orderRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Order", id));

        List<OrderDetailDto> details = orderDetailService.getDetailsByOrderId(order.getId());
        return orderMapper.toDto(order, details);
    }

    /** Create a new order with table validation and WebSocket broadcast. */
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        // Validate IN_PLACE orders have table
        if ("IN_PLACE".equals(request.getType()) && request.getTableId() == null) {
            throw new ConflictException("IN_PLACE orders must have a table");
        }

        // Validate table is available if provided
        RestaurantTable table = null;
        if (request.getTableId() != null) {
            table =
                    tableRepository
                            .findByIdAndRestaurantId(request.getTableId(), restaurantId)
                            .orElseThrow(
                                    () -> new NotFoundException("Table", request.getTableId()));

            if (!"AVAILABLE".equals(table.getStatus())) {
                throw new ConflictException("Table is not available: " + table.getNum());
            }
        }

        // Generate next order number for restaurant
        Integer nextNum = orderRepository.findMaxNumByRestaurantId(restaurantId);
        if (nextNum == null) {
            nextNum = 1;
        } else {
            nextNum++;
        }

        // Create order
        Order order =
                Order.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .num(nextNum)
                        .type(request.getType())
                        .status("PENDING")
                        .total(BigDecimal.ZERO)
                        .people(request.getPeople())
                        .tableId(request.getTableId())
                        .clientId(request.getClientId())
                        .userId(getCurrentUserId())
                        .build();

        order = orderRepository.save(order);

        // Create order details
        BigDecimal total = BigDecimal.ZERO;
        for (com.restaurant.app.order.dto.CreateOrderDetailRequest detailRequest :
                request.getDetails()) {
            OrderDetail detail = orderDetailService.createOrderDetail(order, detailRequest);
            total = total.add(detail.getAmount());
        }

        // Update order total
        order.setTotal(total);
        order = orderRepository.save(order);

        // Update table status if IN_PLACE
        if (table != null) {
            table.setStatus("OCCUPIED");
            tableRepository.save(table);
        }

        // Fetch details for response
        List<OrderDetailDto> details = orderDetailService.getDetailsByOrderId(order.getId());
        OrderDto response = orderMapper.toDto(order, details);

        // Broadcast order creation
        broadcastOrderCreated(response);

        return response;
    }

    /** Update an order (limited fields). */
    @Transactional
    public OrderDto updateOrder(String id, CreateOrderRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        Order order =
                orderRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Order", id));

        // Only allow updating people count and client for now
        if (request.getPeople() != null) {
            order.setPeople(request.getPeople());
        }

        if (request.getClientId() != null) {
            order.setClientId(request.getClientId());
        }

        order = orderRepository.save(order);

        List<OrderDetailDto> details = orderDetailService.getDetailsByOrderId(order.getId());
        return orderMapper.toDto(order, details);
    }

    /** Delete an order (only if PENDING). */
    @Transactional
    public void deleteOrder(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Order order =
                orderRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Order", id));

        if (!"PENDING".equals(order.getStatus())) {
            throw new ConflictException("Can only delete orders in PENDING status");
        }

        // Restore stock for all details
        List<OrderDetail> details = orderDetailRepository.findByOrderId(id);
        for (OrderDetail detail : details) {
            if (!"CANCELLED".equals(detail.getStatus())) {
                // Restore stock (this would call ProductService.updateStock with positive delta)
            }
        }

        orderRepository.delete(order);
    }

    /** Broadcast order creation to WebSocket topic. */
    private void broadcastOrderCreated(OrderDto order) {
        String restaurantId = TenantContext.getRestaurantId();
        messagingTemplate.convertAndSend("/topic/restaurant/" + restaurantId + "/orders", order);
    }

    /** Resolve the current authenticated user's id, if available. */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof UserDetailsAdapter userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}
