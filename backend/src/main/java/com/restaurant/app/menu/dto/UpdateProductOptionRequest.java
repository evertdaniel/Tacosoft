package com.restaurant.app.menu.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** DTO for updating a product option. */
public class UpdateProductOptionRequest {
    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @DecimalMin(value = "0.00", message = "Price adjustment must be positive or zero")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Price adjustment must have up to 2 decimal places")
    private BigDecimal priceAdjustment;

    private Boolean isDefault;

    private Boolean isAvailable;

    public UpdateProductOptionRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPriceAdjustment() {
        return priceAdjustment;
    }

    public void setPriceAdjustment(BigDecimal priceAdjustment) {
        this.priceAdjustment = priceAdjustment;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public Boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
