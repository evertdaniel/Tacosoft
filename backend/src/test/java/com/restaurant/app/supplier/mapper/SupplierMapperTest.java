package com.restaurant.app.supplier.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.supplier.dto.SupplierDto;
import com.restaurant.app.supplier.model.Supplier;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SupplierMapper}. */
class SupplierMapperTest {

    private final SupplierMapper mapper = new SupplierMapper();

    @Test
    void toDto_MapsSupplierEntityToDto() {
        Supplier supplier =
                Supplier.builder()
                        .id("supplier-1")
                        .restaurantId("restaurant-1")
                        .name("Acme")
                        .contactName("John")
                        .email("john@acme.com")
                        .phone("555-1234")
                        .address("123 Main")
                        .taxId("TAX-1")
                        .isActive(true)
                        .build();
        supplier.setCreatedAt(LocalDateTime.of(2026, 6, 22, 10, 0));
        supplier.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 11, 0));

        SupplierDto dto = mapper.toDto(supplier);

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
    }
}
