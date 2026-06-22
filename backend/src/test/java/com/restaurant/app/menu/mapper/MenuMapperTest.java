package com.restaurant.app.menu.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.menu.dto.CategoryDto;
import com.restaurant.app.menu.dto.ProductDto;
import com.restaurant.app.menu.dto.ProductOptionDto;
import com.restaurant.app.menu.dto.ProductionAreaDto;
import com.restaurant.app.menu.dto.SectionDto;
import com.restaurant.app.menu.model.Category;
import com.restaurant.app.menu.model.Product;
import com.restaurant.app.menu.model.ProductOption;
import com.restaurant.app.menu.model.ProductionArea;
import com.restaurant.app.menu.model.Section;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Unit tests for menu mappers. */
class MenuMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final ProductMapper productMapper = new ProductMapper();
    private final ProductOptionMapper productOptionMapper = new ProductOptionMapper();
    private final ProductionAreaMapper productionAreaMapper = new ProductionAreaMapper();
    private final SectionMapper sectionMapper = new SectionMapperImpl();

    @Test
    void categoryMapper_MapsCategoryToDto() {
        Category category =
                Category.builder()
                        .id("category-1")
                        .restaurantId("restaurant-1")
                        .name("Food")
                        .description("Main food")
                        .sectionId("section-1")
                        .isActive(true)
                        .build();
        category.setCreatedAt(LocalDateTime.of(2026, 6, 22, 10, 0));
        category.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 11, 0));

        CategoryDto dto = categoryMapper.toDto(category);

        assertThat(dto.getId()).isEqualTo("category-1");
        assertThat(dto.getName()).isEqualTo("Food");
        assertThat(dto.getSectionId()).isEqualTo("section-1");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 0));
    }

    @Test
    void productMapper_MapsProductToDto() {
        Product product =
                Product.builder()
                        .id("product-1")
                        .restaurantId("restaurant-1")
                        .name("Burger")
                        .description("Tasty")
                        .price(BigDecimal.valueOf(9.99))
                        .categoryId("category-1")
                        .taxRate(BigDecimal.valueOf(0.10))
                        .stock(10)
                        .manageStock(true)
                        .status("AVAILABLE")
                        .imageUrl("image.jpg")
                        .preparationTime(15)
                        .isActive(true)
                        .productionAreaId("area-1")
                        .build();
        product.setCreatedAt(LocalDateTime.of(2026, 6, 22, 10, 0));
        product.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 11, 0));

        ProductDto dto = productMapper.toDto(product);

        assertThat(dto.getId()).isEqualTo("product-1");
        assertThat(dto.getName()).isEqualTo("Burger");
        assertThat(dto.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(9.99));
        assertThat(dto.getCategoryId()).isEqualTo("category-1");
        assertThat(dto.getTaxRate()).isEqualByComparingTo(BigDecimal.valueOf(0.10));
        assertThat(dto.getStock()).isEqualTo(10);
        assertThat(dto.isManageStock()).isTrue();
        assertThat(dto.getStatus()).isEqualTo("AVAILABLE");
        assertThat(dto.getProductionAreaId()).isEqualTo("area-1");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 0));
    }

    @Test
    void productOptionMapper_MapsOptionToDto() {
        ProductOption option =
                ProductOption.builder()
                        .id("option-1")
                        .restaurantId("restaurant-1")
                        .name("Extra cheese")
                        .description("Cheese")
                        .priceAdjustment(BigDecimal.valueOf(0.50))
                        .productId("product-1")
                        .isDefault(false)
                        .isAvailable(true)
                        .build();
        option.setCreatedAt(LocalDateTime.of(2026, 6, 22, 10, 0));
        option.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 11, 0));

        ProductOptionDto dto = productOptionMapper.toDto(option);

        assertThat(dto.getId()).isEqualTo("option-1");
        assertThat(dto.getName()).isEqualTo("Extra cheese");
        assertThat(dto.getPriceAdjustment()).isEqualByComparingTo(BigDecimal.valueOf(0.50));
        assertThat(dto.getProductId()).isEqualTo("product-1");
        assertThat(dto.isDefault()).isFalse();
        assertThat(dto.isAvailable()).isTrue();
    }

    @Test
    void productionAreaMapper_MapsAreaToDto() {
        ProductionArea area =
                ProductionArea.builder()
                        .id("area-1")
                        .restaurantId("restaurant-1")
                        .name("Kitchen")
                        .description("Main kitchen")
                        .isActive(true)
                        .build();
        area.setCreatedAt(LocalDateTime.of(2026, 6, 22, 10, 0));
        area.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 11, 0));

        ProductionAreaDto dto = productionAreaMapper.toDto(area);

        assertThat(dto.getId()).isEqualTo("area-1");
        assertThat(dto.getName()).isEqualTo("Kitchen");
        assertThat(dto.getDescription()).isEqualTo("Main kitchen");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 0));
    }

    @Test
    void sectionMapper_MapsSectionToDto() {
        Section section =
                Section.builder()
                        .id("section-1")
                        .restaurantId("restaurant-1")
                        .name("Lunch")
                        .description("Lunch menu")
                        .displayOrder(1)
                        .isActive(true)
                        .build();

        SectionDto dto = sectionMapper.toDto(section);

        assertThat(dto.id()).isEqualTo("section-1");
        assertThat(dto.restaurantId()).isEqualTo("restaurant-1");
        assertThat(dto.name()).isEqualTo("Lunch");
        assertThat(dto.displayOrder()).isEqualTo(1);
        assertThat(dto.isActive()).isTrue();
    }
}
