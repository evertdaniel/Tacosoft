package com.restaurant.app.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create supplier request DTO. Implements T10.1. */
public class CreateSupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150, message = "Supplier name must not exceed 150 characters")
    private String name;

    @Size(max = 120, message = "Contact name must not exceed 120 characters")
    private String contactName;

    @Email(message = "Email must be valid")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @Size(max = 40, message = "Phone must not exceed 40 characters")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 40, message = "Tax ID must not exceed 40 characters")
    private String taxId;

    public CreateSupplierRequest() {}

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
}
