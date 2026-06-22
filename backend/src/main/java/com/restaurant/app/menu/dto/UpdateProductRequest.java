package com.restaurant.app.menu.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** DTO for updating a product. */
public class UpdateProductRequest {
    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @DecimalMin(value = "0.00", message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price must have up to 2 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.0000", message = "Tax rate must be positive")
    @Digits(integer = 1, fraction = 4, message = "Tax rate must have up to 4 decimal places")
    private BigDecimal taxRate;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private Boolean manageStock;

    @Pattern(
            regexp = "AVAILABLE|OUT_OF_STOCK|OUT_OF_SEASON",
            message = "Status must be AVAILABLE, OUT_OF_STOCK, or OUT_OF_SEASON")
    private String status;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    @Max(value = 180, message = "Preparation time must not exceed 180 minutes")
    private Integer preparationTime;

    private Boolean isActive;

    private String productionAreaId;

    public UpdateProductRequest() {}

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean isManageStock() {
        return manageStock;
    }

    public void setManageStock(Boolean manageStock) {
        this.manageStock = manageStock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(Integer preparationTime) {
        this.preparationTime = preparationTime;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getProductionAreaId() {
        return productionAreaId;
    }

    public void setProductionAreaId(String productionAreaId) {
        this.productionAreaId = productionAreaId;
    }
}
