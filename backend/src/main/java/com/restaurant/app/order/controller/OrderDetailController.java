package com.restaurant.app.order.controller;

import com.restaurant.app.order.dto.OrderDetailDto;
import com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest;
import com.restaurant.app.order.service.OrderDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Controller for order detail endpoints. */
@RestController
@RequestMapping("/order-details")
@Tag(name = "Order Details", description = "Order detail management and status transitions")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    public OrderDetailController(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }

    /** Get an order detail by ID. GET /order-details/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get order detail", description = "Get an order detail by ID")
    public ResponseEntity<OrderDetailDto> getOrderDetailById(@PathVariable String id) {
        OrderDetailDto response = orderDetailService.getOrderDetailById(id);
        return ResponseEntity.ok(response);
    }

    /** Get details by order ID. GET /order-details/order/{orderId} */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "List order details", description = "Get details by order ID")
    public ResponseEntity<List<OrderDetailDto>> getDetailsByOrderId(@PathVariable String orderId) {
        List<OrderDetailDto> response = orderDetailService.getDetailsByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    /** Update order detail status. PUT /order-details/{id}/status */
    @PutMapping("/{id}/status")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('COOK', 'ADMIN')")
    @Operation(
            summary = "Update detail status",
            description = "Update order detail status with transition validation")
    public ResponseEntity<OrderDetailDto> updateOrderDetailStatus(
            @PathVariable String id, @Valid @RequestBody UpdateOrderDetailStatusRequest request) {
        OrderDetailDto response = orderDetailService.updateOrderDetailStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
