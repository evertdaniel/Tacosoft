package com.restaurant.app.supplier.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Unit tests for supplier DTOs. */
class SupplierDtoTest {

    @Test
    void supplierDto_GettersAndSettersAndBuilder() {
        SupplierDto dto =
                SupplierDto.builder()
                        .id("supplier-1")
                        .restaurantId("restaurant-1")
                        .name("Acme")
                        .contactName("John")
                        .email("john@acme.com")
                        .phone("555-1234")
                        .address("123 Main")
                        .taxId("TAX-1")
                        .isActive(true)
                        .createdAt(LocalDateTime.of(2026, 6, 22, 10, 0))
                        .updatedAt(LocalDateTime.of(2026, 6, 22, 11, 0))
                        .build();

        assertThat(dto.getId()).isEqualTo("supplier-1");
        assertThat(dto.getRestaurantId()).isEqualTo("restaurant-1");
        assertThat(dto.getName()).isEqualTo("Acme");
        assertThat(dto.getContactName()).isEqualTo("John");
        assertThat(dto.getEmail()).isEqualTo("john@acme.com");
        assertThat(dto.getPhone()).isEqualTo("555-1234");
        assertThat(dto.getAddress()).isEqualTo("123 Main");
        assertThat(dto.getTaxId()).isEqualTo("TAX-1");
        assertThat(dto.getIsActive()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 0));
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 11, 0));

        dto.setName("New Acme");
        assertThat(dto.getName()).isEqualTo("New Acme");
    }

    @Test
    void createSupplierRequest_GettersAndSetters() {
        CreateSupplierRequest request = new CreateSupplierRequest();
        request.setName("Acme");
        request.setContactName("John");
        request.setEmail("john@acme.com");
        request.setPhone("555-1234");
        request.setAddress("123 Main");
        request.setTaxId("TAX-1");

        assertThat(request.getName()).isEqualTo("Acme");
        assertThat(request.getContactName()).isEqualTo("John");
        assertThat(request.getEmail()).isEqualTo("john@acme.com");
        assertThat(request.getPhone()).isEqualTo("555-1234");
        assertThat(request.getAddress()).isEqualTo("123 Main");
        assertThat(request.getTaxId()).isEqualTo("TAX-1");
    }

    @Test
    void updateSupplierRequest_GettersAndSetters() {
        UpdateSupplierRequest request = new UpdateSupplierRequest();
        request.setName("Acme");
        request.setContactName("John");
        request.setEmail("john@acme.com");
        request.setPhone("555-1234");
        request.setAddress("123 Main");
        request.setTaxId("TAX-1");
        request.setIsActive(false);

        assertThat(request.getName()).isEqualTo("Acme");
        assertThat(request.getContactName()).isEqualTo("John");
        assertThat(request.getEmail()).isEqualTo("john@acme.com");
        assertThat(request.getPhone()).isEqualTo("555-1234");
        assertThat(request.getAddress()).isEqualTo("123 Main");
        assertThat(request.getTaxId()).isEqualTo("TAX-1");
        assertThat(request.getIsActive()).isFalse();
    }
}
