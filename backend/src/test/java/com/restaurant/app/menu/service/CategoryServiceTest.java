package com.restaurant.app.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.dto.CategoryDto;
import com.restaurant.app.menu.dto.CreateCategoryRequest;
import com.restaurant.app.menu.dto.UpdateCategoryRequest;
import com.restaurant.app.menu.mapper.CategoryMapper;
import com.restaurant.app.menu.model.Category;
import com.restaurant.app.menu.repository.CategoryRepository;
import com.restaurant.app.menu.repository.SectionRepository;
import com.restaurant.app.security.TenantContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link CategoryService}. SPEC-MENU-001. */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;

    @Mock private SectionRepository sectionRepository;

    @Mock private CategoryMapper categoryMapper;

    @InjectMocks private CategoryService categoryService;

    private final String restaurantId = "restaurant-1";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createCategory_ValidRequest_SavesAndReturnsDto() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Appetizers");
        request.setDescription("Start your meal");
        request.setSectionId("section-1");

        when(sectionRepository.existsByIdAndRestaurantId("section-1", restaurantId))
                .thenReturn(true);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryDto("category-1"));

        CategoryDto result = categoryService.createCategory(request);

        assertThat(result.getId()).isEqualTo("category-1");
        verify(categoryRepository).save(argThat(c -> c.getName().equals("Appetizers")));
    }

    @Test
    void createCategory_SectionNotFound_ThrowsNotFoundException() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Appetizers");
        request.setSectionId("section-1");

        when(sectionRepository.existsByIdAndRestaurantId("section-1", restaurantId))
                .thenReturn(false);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Section");
    }

    @Test
    void getAllCategories_ReturnsCategoriesForRestaurant() {
        Category category = categoryEntity("category-1", "Appetizers");
        when(categoryRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto("category-1"));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("category-1");
    }

    @Test
    void getActiveCategories_ReturnsOnlyActiveCategories() {
        Category active = categoryEntity("category-1", "Appetizers");
        when(categoryRepository.findByRestaurantIdAndIsActive(restaurantId, true))
                .thenReturn(List.of(active));
        when(categoryMapper.toDto(active)).thenReturn(categoryDto("category-1"));

        List<CategoryDto> result = categoryService.getActiveCategories();

        assertThat(result).hasSize(1);
        verify(categoryRepository).findByRestaurantIdAndIsActive(restaurantId, true);
    }

    @Test
    void getCategoryById_ExistingCategory_ReturnsDto() {
        Category category = categoryEntity("category-1", "Appetizers");
        when(categoryRepository.findByIdAndRestaurantId("category-1", restaurantId))
                .thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto("category-1"));

        CategoryDto result = categoryService.getCategoryById("category-1");

        assertThat(result.getId()).isEqualTo("category-1");
    }

    @Test
    void getCategoryById_CategoryNotFound_ThrowsNotFoundException() {
        when(categoryRepository.findByIdAndRestaurantId("category-1", restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById("category-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateCategory_ExistingCategory_UpdatesAllFields() {
        Category category = categoryEntity("category-1", "Appetizers");
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Mains");
        request.setDescription("Main dishes");
        request.setIsActive(false);

        when(categoryRepository.findByIdAndRestaurantId("category-1", restaurantId))
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto("category-1"));

        CategoryDto result = categoryService.updateCategory("category-1", request);

        assertThat(category.getName()).isEqualTo("Mains");
        assertThat(category.getDescription()).isEqualTo("Main dishes");
        assertThat(category.isActive()).isFalse();
        assertThat(result.getId()).isEqualTo("category-1");
    }

    @Test
    void updateCategory_PartialFields_UpdatesOnlyProvidedFields() {
        Category category = categoryEntity("category-1", "Appetizers");
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Starters");

        when(categoryRepository.findByIdAndRestaurantId("category-1", restaurantId))
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto("category-1"));

        categoryService.updateCategory("category-1", request);

        assertThat(category.getName()).isEqualTo("Starters");
        assertThat(category.getDescription()).isEqualTo("Description");
    }

    @Test
    void updateCategory_CategoryNotFound_ThrowsNotFoundException() {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Mains");

        when(categoryRepository.findByIdAndRestaurantId("category-1", restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory("category-1", request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteCategory_ExistingCategory_DeletesSuccessfully() {
        Category category = categoryEntity("category-1", "Appetizers");
        when(categoryRepository.findByIdAndRestaurantId("category-1", restaurantId))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory("category-1");

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_CategoryNotFound_ThrowsNotFoundException() {
        when(categoryRepository.findByIdAndRestaurantId("category-1", restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory("category-1"))
                .isInstanceOf(NotFoundException.class);
    }

    private Category categoryEntity(String id, String name) {
        return Category.builder()
                .id(id)
                .restaurantId(restaurantId)
                .name(name)
                .description("Description")
                .sectionId("section-1")
                .isActive(true)
                .build();
    }

    private CategoryDto categoryDto(String id) {
        return new CategoryDto(
                id,
                "Category",
                "Description",
                "section-1",
                true,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
