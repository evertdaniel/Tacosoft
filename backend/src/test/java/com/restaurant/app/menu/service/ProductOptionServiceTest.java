package com.restaurant.app.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.dto.CreateProductOptionRequest;
import com.restaurant.app.menu.dto.ProductOptionDto;
import com.restaurant.app.menu.dto.UpdateProductOptionRequest;
import com.restaurant.app.menu.mapper.ProductOptionMapper;
import com.restaurant.app.menu.model.ProductOption;
import com.restaurant.app.menu.repository.ProductOptionRepository;
import com.restaurant.app.menu.repository.ProductRepository;
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

/** Unit tests for {@link ProductOptionService}. */
@ExtendWith(MockitoExtension.class)
class ProductOptionServiceTest {

    @Mock private ProductOptionRepository productOptionRepository;

    @Mock private ProductRepository productRepository;

    @Mock private ProductOptionMapper productOptionMapper;

    @InjectMocks private ProductOptionService productOptionService;

    private final String restaurantId = "restaurant-1";
    private final String productId = "product-1";
    private final String optionId = "option-1";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAllProductOptions_ReturnsOptionsForRestaurant() {
        ProductOption option = optionEntity(optionId, "Extra cheese");
        when(productOptionRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(option));
        when(productOptionMapper.toDto(option)).thenReturn(optionDto(optionId));

        List<ProductOptionDto> result = productOptionService.getAllProductOptions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(optionId);
        verify(productOptionRepository).findByRestaurantId(restaurantId);
    }

    @Test
    void getOptionsByProduct_ReturnsOptionsForProduct() {
        ProductOption option = optionEntity(optionId, "Extra cheese");
        when(productOptionRepository.findByRestaurantIdAndProductId(restaurantId, productId))
                .thenReturn(List.of(option));
        when(productOptionMapper.toDto(option)).thenReturn(optionDto(optionId));

        List<ProductOptionDto> result = productOptionService.getOptionsByProduct(productId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(productId);
        verify(productOptionRepository).findByRestaurantIdAndProductId(restaurantId, productId);
    }

    @Test
    void getAvailableOptionsByProduct_ReturnsOnlyAvailableOptions() {
        ProductOption option = optionEntity(optionId, "Extra cheese");
        option.setAvailable(true);
        when(productOptionRepository.findByRestaurantIdAndProductIdAndIsAvailable(
                        restaurantId, productId, true))
                .thenReturn(List.of(option));
        when(productOptionMapper.toDto(option)).thenReturn(optionDto(optionId));

        List<ProductOptionDto> result =
                productOptionService.getAvailableOptionsByProduct(productId);

        assertThat(result).hasSize(1);
        verify(productOptionRepository)
                .findByRestaurantIdAndProductIdAndIsAvailable(restaurantId, productId, true);
    }

    @Test
    void getProductOptionById_ExistingOption_ReturnsDto() {
        ProductOption option = optionEntity(optionId, "Extra cheese");
        when(productOptionRepository.findByIdAndRestaurantId(optionId, restaurantId))
                .thenReturn(Optional.of(option));
        when(productOptionMapper.toDto(option)).thenReturn(optionDto(optionId));

        ProductOptionDto result = productOptionService.getProductOptionById(optionId);

        assertThat(result.getId()).isEqualTo(optionId);
    }

    @Test
    void getProductOptionById_OptionNotFound_ThrowsNotFoundException() {
        when(productOptionRepository.findByIdAndRestaurantId(optionId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productOptionService.getProductOptionById(optionId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ProductOption");
    }

    @Test
    void createProductOption_ValidRequest_SavesAndReturnsDto() {
        CreateProductOptionRequest request = createRequest();
        when(productRepository.existsByIdAndRestaurantId(productId, restaurantId)).thenReturn(true);
        when(productOptionRepository.save(any(ProductOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productOptionMapper.toDto(any(ProductOption.class))).thenReturn(optionDto(optionId));

        ProductOptionDto result = productOptionService.createProductOption(request);

        assertThat(result.getId()).isEqualTo(optionId);
        verify(productOptionRepository)
                .save(
                        argThat(
                                o ->
                                        o.getName().equals("Extra cheese")
                                                && o.getProductId().equals(productId)));
    }

    @Test
    void createProductOption_ProductNotFound_ThrowsNotFoundException() {
        CreateProductOptionRequest request = createRequest();
        when(productRepository.existsByIdAndRestaurantId(productId, restaurantId))
                .thenReturn(false);

        assertThatThrownBy(() -> productOptionService.createProductOption(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    void updateProductOption_ExistingOption_UpdatesAllFields() {
        ProductOption option = optionEntity(optionId, "Extra cheese");
        UpdateProductOptionRequest request = updateRequest();
        when(productOptionRepository.findByIdAndRestaurantId(optionId, restaurantId))
                .thenReturn(Optional.of(option));
        when(productOptionRepository.save(any(ProductOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productOptionMapper.toDto(option)).thenReturn(optionDto(optionId));

        ProductOptionDto result = productOptionService.updateProductOption(optionId, request);

        assertThat(option.getName()).isEqualTo("Bacon");
        assertThat(option.getDescription()).isEqualTo("Crispy bacon");
        assertThat(option.getPriceAdjustment()).isEqualByComparingTo(BigDecimal.valueOf(1.50));
        assertThat(option.isDefault()).isTrue();
        assertThat(option.isAvailable()).isFalse();
        assertThat(result.getId()).isEqualTo(optionId);
    }

    @Test
    void updateProductOption_PartialFields_UpdatesOnlyProvidedFields() {
        ProductOption option = optionEntity(optionId, "Extra cheese");
        UpdateProductOptionRequest request = new UpdateProductOptionRequest();
        request.setName("Bacon");

        when(productOptionRepository.findByIdAndRestaurantId(optionId, restaurantId))
                .thenReturn(Optional.of(option));
        when(productOptionRepository.save(any(ProductOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productOptionMapper.toDto(option)).thenReturn(optionDto(optionId));

        productOptionService.updateProductOption(optionId, request);

        assertThat(option.getName()).isEqualTo("Bacon");
        assertThat(option.getDescription()).isEqualTo("Description");
        assertThat(option.isAvailable()).isTrue();
    }

    @Test
    void updateProductOption_OptionNotFound_ThrowsNotFoundException() {
        UpdateProductOptionRequest request = new UpdateProductOptionRequest();
        request.setName("Bacon");
        when(productOptionRepository.findByIdAndRestaurantId(optionId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productOptionService.updateProductOption(optionId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ProductOption");
    }

    @Test
    void deleteProductOption_ExistingOption_DeletesSuccessfully() {
        ProductOption option = optionEntity(optionId, "Extra cheese");
        when(productOptionRepository.findByIdAndRestaurantId(optionId, restaurantId))
                .thenReturn(Optional.of(option));

        productOptionService.deleteProductOption(optionId);

        verify(productOptionRepository).delete(option);
    }

    @Test
    void deleteProductOption_OptionNotFound_ThrowsNotFoundException() {
        when(productOptionRepository.findByIdAndRestaurantId(optionId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productOptionService.deleteProductOption(optionId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ProductOption");
    }

    private CreateProductOptionRequest createRequest() {
        CreateProductOptionRequest request = new CreateProductOptionRequest();
        request.setName("Extra cheese");
        request.setDescription("Cheese topping");
        request.setPriceAdjustment(BigDecimal.valueOf(0.50));
        request.setProductId(productId);
        request.setDefault(false);
        request.setAvailable(true);
        return request;
    }

    private UpdateProductOptionRequest updateRequest() {
        UpdateProductOptionRequest request = new UpdateProductOptionRequest();
        request.setName("Bacon");
        request.setDescription("Crispy bacon");
        request.setPriceAdjustment(BigDecimal.valueOf(1.50));
        request.setDefault(true);
        request.setAvailable(false);
        return request;
    }

    private ProductOption optionEntity(String id, String name) {
        return ProductOption.builder()
                .id(id)
                .restaurantId(restaurantId)
                .name(name)
                .description("Description")
                .priceAdjustment(BigDecimal.valueOf(0.50))
                .productId(productId)
                .isDefault(false)
                .isAvailable(true)
                .build();
    }

    private ProductOptionDto optionDto(String id) {
        return new ProductOptionDto(
                id,
                "Option",
                "Description",
                BigDecimal.valueOf(0.50),
                productId,
                false,
                true,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
