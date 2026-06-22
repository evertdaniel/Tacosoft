package com.restaurant.app.menu.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** ProductionArea entity - kitchen/bar zones for routing order details. */
@Entity
@Table(name = "production_area")
public class ProductionArea extends TenantAware {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Default constructor
    public ProductionArea() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public static class Builder {
        private final ProductionArea area = new ProductionArea();

        public Builder id(String id) {
            area.id = id;
            return this;
        }

        public Builder name(String name) {
            area.name = name;
            return this;
        }

        public Builder description(String description) {
            area.description = description;
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            area.setRestaurantId(restaurantId);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            area.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            area.updatedAt = updatedAt;
            return this;
        }

        public Builder isActive(boolean active) {
            area.isActive = active;
            return this;
        }

        public ProductionArea build() {
            return area;
        }
    }
}
