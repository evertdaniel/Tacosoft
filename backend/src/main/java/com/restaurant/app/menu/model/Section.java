package com.restaurant.app.menu.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;

/** Section entity - groups menu categories (e.g., "Lunch", "Dinner", "Drinks"). */
@Entity
@Table(name = "section")
public class Section extends TenantAware {

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Default constructor
    public Section() {}

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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public static class Builder {
        private final Section section = new Section();

        public Builder id(String id) {
            section.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            section.setRestaurantId(restaurantId);
            return this;
        }

        public Builder name(String name) {
            section.name = name;
            return this;
        }

        public Builder description(String description) {
            section.description = description;
            return this;
        }

        public Builder displayOrder(Integer displayOrder) {
            section.displayOrder = displayOrder;
            return this;
        }

        public Builder isPublic(boolean isPublic) {
            section.isPublic = isPublic;
            return this;
        }

        public Builder isActive(boolean active) {
            section.isActive = active;
            return this;
        }

        public Section build() {
            return section;
        }
    }
}
