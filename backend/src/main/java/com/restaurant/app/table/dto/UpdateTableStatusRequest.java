package com.restaurant.app.table.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** DTO for updating table status. */
public class UpdateTableStatusRequest {
    @NotNull(message = "Status is required") @Pattern(
            regexp = "AVAILABLE|OCCUPIED|RESERVED|CLEANING",
            message = "Status must be AVAILABLE, OCCUPIED, RESERVED, or CLEANING")
    private String status;

    public UpdateTableStatusRequest() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
