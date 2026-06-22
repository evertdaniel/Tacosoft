package com.restaurant.app.menu.service;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.dto.CategoryDto;
import com.restaurant.app.menu.dto.CreateCategoryRequest;
import com.restaurant.app.menu.dto.UpdateCategoryRequest;
import com.restaurant.app.menu.mapper.CategoryMapper;
import com.restaurant.app.menu.model.Category;
import com.restaurant.app.menu.repository.CategoryRepository;
import com.restaurant.app.menu.repository.SectionRepository;
import com.restaurant.app.security.TenantContext;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for category operations. */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(
            CategoryRepository categoryRepository,
            SectionRepository sectionRepository,
            CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.sectionRepository = sectionRepository;
        this.categoryMapper = categoryMapper;
    }

    /** Get all categories for current restaurant. */
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        String restaurantId = TenantContext.getRestaurantId();
        return categoryRepository.findByRestaurantId(restaurantId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    /** Get active categories only. */
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        String restaurantId = TenantContext.getRestaurantId();
        return categoryRepository.findByRestaurantIdAndIsActive(restaurantId, true).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    /** Get a category by ID. */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Category category =
                categoryRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Category", id));
        return categoryMapper.toDto(category);
    }

    /** Create a new category. */
    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        // Validate section exists
        if (!sectionRepository.existsByIdAndRestaurantId(request.getSectionId(), restaurantId)) {
            throw new NotFoundException("Section", request.getSectionId());
        }

        Category category =
                Category.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .name(request.getName())
                        .description(request.getDescription())
                        .sectionId(request.getSectionId())
                        .isActive(true)
                        .build();

        category = categoryRepository.save(category);
        return categoryMapper.toDto(category);
    }

    /** Update a category. */
    @Transactional
    public CategoryDto updateCategory(String id, UpdateCategoryRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        Category category =
                categoryRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Category", id));

        if (request.getName() != null) {
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            category.setActive(request.getIsActive());
        }

        category = categoryRepository.save(category);
        return categoryMapper.toDto(category);
    }

    /** Delete a category. */
    @Transactional
    public void deleteCategory(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Category category =
                categoryRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Category", id));
        categoryRepository.delete(category);
    }
}
