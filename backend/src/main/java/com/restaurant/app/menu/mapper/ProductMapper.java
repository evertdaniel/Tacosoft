package com.restaurant.app.menu.mapper;

import com.restaurant.app.menu.dto.ProductDto;
import com.restaurant.app.menu.model.Product;
import org.springframework.stereotype.Component;

/** Mapper for Product entity and DTOs. */
@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategoryId(),
                product.getTaxRate(),
                product.getStock(),
                product.isManageStock(),
                product.getStatus(),
                product.getImageUrl(),
                product.getPreparationTime(),
                product.isActive(),
                product.getProductionAreaId(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
