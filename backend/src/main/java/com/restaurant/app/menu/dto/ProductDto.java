package com.restaurant.app.menu.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO for product responses. */
public class ProductDto {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryId;
    private BigDecimal taxRate;
    private Integer stock;
    private boolean manageStock;
    private String status;
    private String imageUrl;
    private Integer preparationTime;
    private boolean isActive;
    private String productionAreaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductDto() {}

    public ProductDto(
            String id,
            String name,
            String description,
            BigDecimal price,
            String categoryId,
            BigDecimal taxRate,
            Integer stock,
            boolean manageStock,
            String status,
            String imageUrl,
            Integer preparationTime,
            boolean isActive,
            String productionAreaId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.taxRate = taxRate;
        this.stock = stock;
        this.manageStock = manageStock;
        this.status = status;
        this.imageUrl = imageUrl;
        this.preparationTime = preparationTime;
        this.isActive = isActive;
        this.productionAreaId = productionAreaId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
