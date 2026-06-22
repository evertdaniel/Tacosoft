package com.restaurant.app.menu.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** DTO for creating a product. */
public class CreateProductRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Price is required") @DecimalMin(value = "0.00", message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price must have up to 2 decimal places")
    private BigDecimal price;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotNull(message = "Tax rate is required") @DecimalMin(value = "0.0000", message = "Tax rate must be positive")
    @Digits(integer = 1, fraction = 4, message = "Tax rate must have up to 4 decimal places")
    private BigDecimal taxRate = BigDecimal.ZERO;

    @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock = 0;

    private boolean manageStock = false;

    @Pattern(
            regexp = "AVAILABLE|OUT_OF_STOCK|OUT_OF_SEASON",
            message = "Status must be AVAILABLE, OUT_OF_STOCK, or OUT_OF_SEASON")
    private String status = "AVAILABLE";

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    @NotNull(message = "Preparation time is required") @Min(value = 1, message = "Preparation time must be at least 1 minute")
    @Max(value = 180, message = "Preparation time must not exceed 180 minutes")
    private Integer preparationTime = 15;

    private boolean isActive = true;

    private String productionAreaId;

    public CreateProductRequest() {}

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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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

    public boolean isManageStock() {
        return manageStock;
    }

    public void setManageStock(boolean manageStock) {
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getProductionAreaId() {
        return productionAreaId;
    }

    public void setProductionAreaId(String productionAreaId) {
        this.productionAreaId = productionAreaId;
    }
}
