package com.restaurant.app.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** DTO for updating order detail status. */
public class UpdateOrderDetailStatusRequest {
    @NotNull(message = "Status is required") @Pattern(
            regexp = "PENDING|IN_PROGRESS|READY|DELIVERED|CANCELLED",
            message = "Status must be PENDING, IN_PROGRESS, READY, DELIVERED, or CANCELLED")
    private String status;

    public UpdateOrderDetailStatusRequest() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
