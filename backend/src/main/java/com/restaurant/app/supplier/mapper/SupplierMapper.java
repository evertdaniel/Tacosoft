package com.restaurant.app.supplier.mapper;

import com.restaurant.app.supplier.dto.SupplierDto;
import com.restaurant.app.supplier.model.Supplier;
import org.springframework.stereotype.Component;

/** Mapper for Supplier entity and DTOs. Implements T10.1. */
@Component
public class SupplierMapper {

    /** Convert Supplier entity to SupplierDto. */
    public SupplierDto toDto(Supplier supplier) {
        return SupplierDto.builder()
                .id(supplier.getId())
                .restaurantId(supplier.getRestaurantId())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .taxId(supplier.getTaxId())
                .isActive(supplier.getIsActive())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
