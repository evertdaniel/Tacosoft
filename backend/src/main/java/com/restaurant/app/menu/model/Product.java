package com.restaurant.app.menu.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Product entity - menu item with pricing, stock, and tax. */
@Entity
@Table(name = "product")
public class Product extends TenantAware {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "category_id", nullable = false, columnDefinition = "CHAR(36)")
    private String categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private Category category;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "manage_stock", nullable = false)
    private boolean manageStock = false;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "AVAILABLE";

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "preparation_time", nullable = false)
    private Integer preparationTime = 15;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductOption> options = new ArrayList<>();

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

    @Column(name = "production_area_id", columnDefinition = "CHAR(36)")
    private String productionAreaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "production_area_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private ProductionArea productionArea;

    // Default constructor
    public Product() {}

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public List<ProductOption> getOptions() {
        return options;
    }

    public void setOptions(List<ProductOption> options) {
        this.options = options;
    }

    public String getProductionAreaId() {
        return productionAreaId;
    }

    public void setProductionAreaId(String productionAreaId) {
        this.productionAreaId = productionAreaId;
    }

    public ProductionArea getProductionArea() {
        return productionArea;
    }

    public void setProductionArea(ProductionArea productionArea) {
        this.productionArea = productionArea;
    }

    public static class Builder {
        private final Product product = new Product();

        public Builder id(String id) {
            product.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            product.setRestaurantId(restaurantId);
            return this;
        }

        public Builder name(String name) {
            product.name = name;
            return this;
        }

        public Builder description(String description) {
            product.description = description;
            return this;
        }

        public Builder price(BigDecimal price) {
            product.price = price;
            return this;
        }

        public Builder unitCost(BigDecimal unitCost) {
            product.unitCost = unitCost;
            return this;
        }

        public Builder categoryId(String categoryId) {
            product.categoryId = categoryId;
            return this;
        }

        public Builder taxRate(BigDecimal taxRate) {
            product.taxRate = taxRate;
            return this;
        }

        public Builder stock(Integer stock) {
            product.stock = stock;
            return this;
        }

        public Builder manageStock(boolean manageStock) {
            product.manageStock = manageStock;
            return this;
        }

        public Builder status(String status) {
            product.status = status;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            product.imageUrl = imageUrl;
            return this;
        }

        public Builder preparationTime(Integer preparationTime) {
            product.preparationTime = preparationTime;
            return this;
        }

        public Builder isActive(boolean active) {
            product.isActive = active;
            return this;
        }

        public Builder productionAreaId(String productionAreaId) {
            product.productionAreaId = productionAreaId;
            return this;
        }

        public Product build() {
            return product;
        }
    }
}
