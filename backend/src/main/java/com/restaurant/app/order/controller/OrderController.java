package com.restaurant.app.order.controller;

import com.restaurant.app.order.dto.CreateOrderRequest;
import com.restaurant.app.order.dto.OrderDto;
import com.restaurant.app.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for order CRUD endpoints. Implements SPEC-ORDER-001, SPEC-ORDER-002, INV-01, INV-04.
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management and status tracking")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Create a new order. POST /orders */
    @PostMapping
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('WAITER', 'ADMIN')")
    @Operation(
            summary = "Create order",
            description =
                    "Create a new order with table validation and WebSocket broadcast. Validates"
                        + " table availability for IN_PLACE orders. Generates unique order number"
                        + " per restaurant (INV-01).")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Order created successfully",
                        content = @Content(schema = @Schema(implementation = OrderDto.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request data or table unavailable"),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token required"),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - insufficient permissions")
            })
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDto response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/orders/" + response.getId()))
                .body(response);
    }

    /** Get all orders for current restaurant. GET /orders */
    @GetMapping
    @Operation(
            summary = "List all orders",
            description = "Get all orders for the current restaurant (tenant-isolated)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token required"),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - insufficient permissions")
            })
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> response = orderService.getAllOrders();
        return ResponseEntity.ok(response);
    }

    /** Get active orders. GET /orders/active */
    @GetMapping("/active")
    @Operation(
            summary = "List active orders",
            description = "Get active orders (not completed/cancelled)")
    public ResponseEntity<List<OrderDto>> getActiveOrders() {
        List<OrderDto> response = orderService.getActiveOrders();
        return ResponseEntity.ok(response);
    }

    /** Get orders by status. GET /orders/status/{status} */
    @GetMapping("/status/{status}")
    @Operation(summary = "List orders by status", description = "Get orders by status")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) {
        List<OrderDto> response = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(response);
    }

    /** Get an order by ID. GET /orders/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get order", description = "Get an order by ID")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String id) {
        OrderDto response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    /** Update an order. PUT /orders/{id} */
    @PutMapping("/{id}")
    @Operation(summary = "Update order", description = "Update an order by ID")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable String id, @Valid @RequestBody CreateOrderRequest request) {
        OrderDto response = orderService.updateOrder(id, request);
        return ResponseEntity.ok(response);
    }

    /** Delete an order. DELETE /orders/{id} */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete order",
            description = "Delete an order by ID (only allowed if order status is PENDING)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Order cannot be deleted (not in PENDING status)"),
                @ApiResponse(responseCode = "404", description = "Order not found"),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token required"),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - insufficient permissions")
            })
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
