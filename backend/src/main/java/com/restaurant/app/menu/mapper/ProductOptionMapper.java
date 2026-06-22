package com.restaurant.app.menu.mapper;

import com.restaurant.app.menu.dto.ProductOptionDto;
import com.restaurant.app.menu.model.ProductOption;
import org.springframework.stereotype.Component;

/** Mapper for ProductOption entity and DTOs. */
@Component
public class ProductOptionMapper {

    public ProductOptionDto toDto(ProductOption option) {
        return new ProductOptionDto(
                option.getId(),
                option.getName(),
                option.getDescription(),
                option.getPriceAdjustment(),
                option.getProductId(),
                option.isDefault(),
                option.isAvailable(),
                option.getCreatedAt(),
                option.getUpdatedAt());
    }
}
