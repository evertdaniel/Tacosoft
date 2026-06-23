package com.restaurant.app.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.dto.CreateProductRequest;
import com.restaurant.app.menu.dto.ProductDto;
import com.restaurant.app.menu.dto.UpdateProductRequest;
import com.restaurant.app.menu.mapper.ProductMapper;
import com.restaurant.app.menu.model.Product;
import com.restaurant.app.menu.repository.CategoryRepository;
import com.restaurant.app.menu.repository.ProductRepository;
import com.restaurant.app.menu.repository.ProductionAreaRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
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

/** Unit tests for {@link ProductService}. SPEC-MENU-001. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;

    @Mock private CategoryRepository categoryRepository;

    @Mock private ProductionAreaRepository productionAreaRepository;

    @Mock private ProductMapper productMapper;

    @InjectMocks private ProductService productService;

    private final String restaurantId = "restaurant-1";
    private final String categoryId = "category-1";
    private final String productId = "product-1";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAllProducts_ReturnsProductsForRestaurant() {
        Product product = productEntity(productId, "Burger");
        when(productRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto(productId));

        List<ProductDto> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(productId);
        verify(productRepository).findByRestaurantId(restaurantId);
    }

    @Test
    void getActiveProducts_ReturnsOnlyActiveProducts() {
        Product active = productEntity(productId, "Burger");
        when(productRepository.findByRestaurantIdAndIsActive(restaurantId, true))
                .thenReturn(List.of(active));
        when(productMapper.toDto(active)).thenReturn(productDto(productId));

        List<ProductDto> result = productService.getActiveProducts();

        assertThat(result).hasSize(1);
        verify(productRepository).findByRestaurantIdAndIsActive(restaurantId, true);
    }

    @Test
    void getAvailableProducts_ReturnsActiveAvailableProducts() {
        Product available = productEntity(productId, "Burger");
        available.setStatus("AVAILABLE");
        when(productRepository.findByRestaurantIdAndIsActiveAndStatus(
                        restaurantId, true, "AVAILABLE"))
                .thenReturn(List.of(available));
        when(productMapper.toDto(available)).thenReturn(productDto(productId));

        List<ProductDto> result = productService.getAvailableProducts();

        assertThat(result).hasSize(1);
        verify(productRepository)
                .findByRestaurantIdAndIsActiveAndStatus(restaurantId, true, "AVAILABLE");
    }

    @Test
    void getProductsByCategory_ReturnsProductsForCategory() {
        Product product = productEntity(productId, "Burger");
        when(productRepository.findByRestaurantIdAndCategoryId(restaurantId, categoryId))
                .thenReturn(List.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto(productId));

        List<ProductDto> result = productService.getProductsByCategory(categoryId);

        assertThat(result).hasSize(1);
        verify(productRepository).findByRestaurantIdAndCategoryId(restaurantId, categoryId);
    }

    @Test
    void getProductById_ExistingProduct_ReturnsDto() {
        Product product = productEntity(productId, "Burger");
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto(productId));

        ProductDto result = productService.getProductById(productId);

        assertThat(result.getId()).isEqualTo(productId);
    }

    @Test
    void getProductById_ProductNotFound_ThrowsNotFoundException() {
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createProduct_ValidRequest_SavesAndReturnsDto() {
        CreateProductRequest request = createRequest();
        when(categoryRepository.existsByIdAndRestaurantId(categoryId, restaurantId))
                .thenReturn(true);
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto(productId));

        ProductDto result = productService.createProduct(request);

        assertThat(result.getId()).isEqualTo(productId);
        verify(productRepository).save(argThat(p -> p.getName().equals("Burger")));
    }

    @Test
    void createProduct_CategoryNotFound_ThrowsNotFoundException() {
        CreateProductRequest request = createRequest();
        when(categoryRepository.existsByIdAndRestaurantId(categoryId, restaurantId))
                .thenReturn(false);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    void createProduct_ProductionAreaNotFound_ThrowsNotFoundException() {
        CreateProductRequest request = createRequest();
        request.setProductionAreaId("area-1");
        when(categoryRepository.existsByIdAndRestaurantId(categoryId, restaurantId))
                .thenReturn(true);
        when(productionAreaRepository.existsByIdAndRestaurantId("area-1", restaurantId))
                .thenReturn(false);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ProductionArea");
    }

    @Test
    void createProduct_ManageStockWithZeroStock_ThrowsConflictException() {
        CreateProductRequest request = createRequest();
        request.setManageStock(true);
        request.setStock(0);
        when(categoryRepository.existsByIdAndRestaurantId(categoryId, restaurantId))
                .thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("initial stock > 0");
    }

    @Test
    void updateProduct_ExistingProduct_UpdatesAllFields() {
        Product product = productEntity(productId, "Burger");
        UpdateProductRequest request = updateRequest();
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDto(product)).thenReturn(productDto(productId));

        ProductDto result = productService.updateProduct(productId, request);

        assertThat(product.getName()).isEqualTo("Cheeseburger");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(12.99));
        assertThat(product.getTaxRate()).isEqualByComparingTo(BigDecimal.valueOf(0.2100));
        assertThat(product.getStock()).isEqualTo(20);
        assertThat(product.isManageStock()).isTrue();
        assertThat(product.getStatus()).isEqualTo("AVAILABLE");
        assertThat(product.getImageUrl()).isEqualTo("http://image");
        assertThat(product.getPreparationTime()).isEqualTo(10);
        assertThat(product.isActive()).isFalse();
        assertThat(product.getProductionAreaId()).isEqualTo("area-1");
        assertThat(result.getId()).isEqualTo(productId);
    }

    @Test
    void updateProduct_PartialFields_UpdatesOnlyProvidedFields() {
        Product product = productEntity(productId, "Burger");
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Cheeseburger");

        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDto(product)).thenReturn(productDto(productId));

        productService.updateProduct(productId, request);

        assertThat(product.getName()).isEqualTo("Cheeseburger");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(9.99));
    }

    @Test
    void updateProduct_NegativeStockWithManagement_ThrowsConflictException() {
        Product product = productEntity(productId, "Burger");
        product.setManageStock(true);
        UpdateProductRequest request = new UpdateProductRequest();
        request.setStock(-1);

        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Stock cannot be negative");
    }

    @Test
    void updateProduct_ProductionAreaNotFound_ThrowsNotFoundException() {
        Product product = productEntity(productId, "Burger");
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductionAreaId("area-1");

        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));
        when(productionAreaRepository.existsByIdAndRestaurantId("area-1", restaurantId))
                .thenReturn(false);

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ProductionArea");
    }

    @Test
    void updateProduct_ProductNotFound_ThrowsNotFoundException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Cheeseburger");
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteProduct_ExistingProduct_DeletesSuccessfully() {
        Product product = productEntity(productId, "Burger");
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_ProductNotFound_ThrowsNotFoundException() {
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStock_ProductWithoutStockManagement_DoesNothing() {
        Product product = productEntity(productId, "Burger");
        product.setManageStock(false);
        product.setStock(10);
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));

        productService.updateStock(productId, -5);

        assertThat(product.getStock()).isEqualTo(10);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateStock_SufficientStock_ReducesStock() {
        Product product = productEntity(productId, "Burger");
        product.setManageStock(true);
        product.setStock(10);
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.updateStock(productId, -3);

        assertThat(product.getStock()).isEqualTo(7);
        verify(productRepository).save(product);
    }

    @Test
    void updateStock_ReachesZero_SetsStatusOutOfStock() {
        Product product = productEntity(productId, "Burger");
        product.setManageStock(true);
        product.setStock(2);
        product.setStatus("AVAILABLE");
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.updateStock(productId, -2);

        assertThat(product.getStock()).isEqualTo(0);
        assertThat(product.getStatus()).isEqualTo("OUT_OF_STOCK");
    }

    @Test
    void updateStock_InsufficientStock_ThrowsConflictException() {
        Product product = productEntity(productId, "Burger");
        product.setManageStock(true);
        product.setStock(1);
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateStock(productId, -5))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void updateStock_ProductNotFound_ThrowsNotFoundException() {
        when(productRepository.findByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateStock(productId, -1))
                .isInstanceOf(NotFoundException.class);
    }

    private CreateProductRequest createRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Burger");
        request.setDescription("Beef burger");
        request.setPrice(BigDecimal.valueOf(9.99));
        request.setCategoryId(categoryId);
        request.setTaxRate(BigDecimal.valueOf(0.1000));
        request.setStock(10);
        request.setManageStock(false);
        request.setStatus("AVAILABLE");
        request.setPreparationTime(15);
        request.setActive(true);
        return request;
    }

    private UpdateProductRequest updateRequest() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Cheeseburger");
        request.setDescription("Cheesy");
        request.setPrice(BigDecimal.valueOf(12.99));
        request.setTaxRate(BigDecimal.valueOf(0.2100));
        request.setStock(20);
        request.setManageStock(true);
        request.setStatus("AVAILABLE");
        request.setImageUrl("http://image");
        request.setPreparationTime(10);
        request.setActive(false);
        request.setProductionAreaId("area-1");
        when(productionAreaRepository.existsByIdAndRestaurantId("area-1", restaurantId))
                .thenReturn(true);
        return request;
    }

    private Product productEntity(String id, String name) {
        return Product.builder()
                .id(id)
                .restaurantId(restaurantId)
                .name(name)
                .description("Description")
                .price(BigDecimal.valueOf(9.99))
                .categoryId(categoryId)
                .taxRate(BigDecimal.valueOf(0.1000))
                .stock(10)
                .manageStock(false)
                .status("AVAILABLE")
                .preparationTime(15)
                .isActive(true)
                .build();
    }

    private ProductDto productDto(String id) {
        return new ProductDto(
                id,
                "Product",
                "Description",
                BigDecimal.valueOf(9.99),
                categoryId,
                BigDecimal.valueOf(0.1000),
                10,
                true,
                "AVAILABLE",
                null,
                15,
                true,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
