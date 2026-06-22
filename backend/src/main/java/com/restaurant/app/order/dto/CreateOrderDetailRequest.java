package com.restaurant.app.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTO for creating an order detail. */
public class CreateOrderDetailRequest {
    @NotNull(message = "Product ID is required") private String productId;

    @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String productOptionId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;

    public CreateOrderDetailRequest() {}

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductOptionId() {
        return productOptionId;
    }

    public void setProductOptionId(String productOptionId) {
        this.productOptionId = productOptionId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
