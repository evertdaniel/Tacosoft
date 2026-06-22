package com.restaurant.app.menu.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductOption entity - customizable options for products (e.g., "Extra cheese", "Large size").
 */
@Entity
@Table(name = "product_option")
public class ProductOption extends TenantAware {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "price_adjustment", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Column(name = "product_id", nullable = false, columnDefinition = "CHAR(36)")
    private String productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private Product product;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

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

    // Default constructor
    public ProductOption() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
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

    public BigDecimal getPriceAdjustment() {
        return priceAdjustment;
    }

    public void setPriceAdjustment(BigDecimal priceAdjustment) {
        this.priceAdjustment = priceAdjustment;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public static class Builder {
        private final ProductOption option = new ProductOption();

        public Builder id(String id) {
            option.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            option.setRestaurantId(restaurantId);
            return this;
        }

        public Builder name(String name) {
            option.name = name;
            return this;
        }

        public Builder description(String description) {
            option.description = description;
            return this;
        }

        public Builder priceAdjustment(BigDecimal priceAdjustment) {
            option.priceAdjustment = priceAdjustment;
            return this;
        }

        public Builder productId(String productId) {
            option.productId = productId;
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            option.isDefault = isDefault;
            return this;
        }

        public Builder isAvailable(boolean isAvailable) {
            option.isAvailable = isAvailable;
            return this;
        }

        public ProductOption build() {
            return option;
        }
    }
}
