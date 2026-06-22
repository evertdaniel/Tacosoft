package com.restaurant.app.menu.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Category entity - groups products within a section (e.g., "Appetizers", "Main Course"). */
@Entity
@Table(name = "category")
public class Category extends TenantAware {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "section_id", nullable = false, columnDefinition = "CHAR(36)")
    private String sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "section_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private Section section;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

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
    public Category() {}

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

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public static class Builder {
        private final Category category = new Category();

        public Builder id(String id) {
            category.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            category.setRestaurantId(restaurantId);
            return this;
        }

        public Builder name(String name) {
            category.name = name;
            return this;
        }

        public Builder description(String description) {
            category.description = description;
            return this;
        }

        public Builder sectionId(String sectionId) {
            category.sectionId = sectionId;
            return this;
        }

        public Builder section(Section section) {
            category.section = section;
            return this;
        }

        public Builder isActive(boolean active) {
            category.isActive = active;
            return this;
        }

        public Category build() {
            return category;
        }
    }
}
