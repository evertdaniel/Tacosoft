package com.restaurant.app.menu.mapper;

import com.restaurant.app.menu.dto.ProductionAreaDto;
import com.restaurant.app.menu.model.ProductionArea;
import org.springframework.stereotype.Component;

/** Mapper for ProductionArea entity and DTOs. */
@Component
public class ProductionAreaMapper {

    public ProductionAreaDto toDto(ProductionArea area) {
        return new ProductionAreaDto(
                area.getId(),
                area.getName(),
                area.getDescription(),
                area.getCreatedAt(),
                area.getUpdatedAt());
    }
}
