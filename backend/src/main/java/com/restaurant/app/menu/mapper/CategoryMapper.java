package com.restaurant.app.menu.mapper;

import com.restaurant.app.menu.dto.CategoryDto;
import com.restaurant.app.menu.model.Category;
import org.springframework.stereotype.Component;

/** Mapper for Category entity and DTOs. */
@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSectionId(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
