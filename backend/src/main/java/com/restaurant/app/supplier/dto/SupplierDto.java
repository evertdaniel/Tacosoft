package com.restaurant.app.supplier.dto;

import java.time.LocalDateTime;

/** Supplier DTO. Implements T10.1. */
public class SupplierDto {

    private String id;
    private String restaurantId;
    private String name;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private String taxId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SupplierDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SupplierDto dto = new SupplierDto();

        public Builder id(String id) {
            dto.id = id;
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            dto.restaurantId = restaurantId;
            return this;
        }

        public Builder name(String name) {
            dto.name = name;
            return this;
        }

        public Builder contactName(String contactName) {
            dto.contactName = contactName;
            return this;
        }

        public Builder email(String email) {
            dto.email = email;
            return this;
        }

        public Builder phone(String phone) {
            dto.phone = phone;
            return this;
        }

        public Builder address(String address) {
            dto.address = address;
            return this;
        }

        public Builder taxId(String taxId) {
            dto.taxId = taxId;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            dto.isActive = isActive;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            dto.updatedAt = updatedAt;
            return this;
        }

        public SupplierDto build() {
            return dto;
        }
    }
}
