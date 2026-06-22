package com.restaurant.app.supplier.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;

/** Supplier entity - product and service suppliers. Implements SDD §6.3 and T10.1. */
@Entity
@Table(name = "supplier")
public class Supplier extends TenantAware {

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "contact_name", length = 120)
    private String contactName;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "tax_id", length = 40)
    private String taxId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Supplier() {}

    public static Builder builder() {
        return new Builder();
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @PrePersist
    protected void onCreate() {
        setCreatedAt(java.time.LocalDateTime.now());
        setUpdatedAt(java.time.LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedAt(java.time.LocalDateTime.now());
    }

    public static class Builder {
        private final Supplier supplier = new Supplier();

        public Builder id(String id) {
            supplier.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            supplier.setRestaurantId(restaurantId);
            return this;
        }

        public Builder name(String name) {
            supplier.name = name;
            return this;
        }

        public Builder contactName(String contactName) {
            supplier.contactName = contactName;
            return this;
        }

        public Builder email(String email) {
            supplier.email = email;
            return this;
        }

        public Builder phone(String phone) {
            supplier.phone = phone;
            return this;
        }

        public Builder address(String address) {
            supplier.address = address;
            return this;
        }

        public Builder taxId(String taxId) {
            supplier.taxId = taxId;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            supplier.isActive = isActive;
            return this;
        }

        public Supplier build() {
            return supplier;
        }
    }
}
