package com.restaurant.app.menu.service;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.dto.CreateProductOptionRequest;
import com.restaurant.app.menu.dto.ProductOptionDto;
import com.restaurant.app.menu.dto.UpdateProductOptionRequest;
import com.restaurant.app.menu.mapper.ProductOptionMapper;
import com.restaurant.app.menu.model.ProductOption;
import com.restaurant.app.menu.repository.ProductOptionRepository;
import com.restaurant.app.menu.repository.ProductRepository;
import com.restaurant.app.security.TenantContext;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for product option operations. */
@Service
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;
    private final ProductOptionMapper productOptionMapper;

    public ProductOptionService(
            ProductOptionRepository productOptionRepository,
            ProductRepository productRepository,
            ProductOptionMapper productOptionMapper) {
        this.productOptionRepository = productOptionRepository;
        this.productRepository = productRepository;
        this.productOptionMapper = productOptionMapper;
    }

    /** Get all options for current restaurant. */
    @Transactional(readOnly = true)
    public List<ProductOptionDto> getAllProductOptions() {
        String restaurantId = TenantContext.getRestaurantId();
        return productOptionRepository.findByRestaurantId(restaurantId).stream()
                .map(productOptionMapper::toDto)
                .toList();
    }

    /** Get options by product. */
    @Transactional(readOnly = true)
    public List<ProductOptionDto> getOptionsByProduct(String productId) {
        String restaurantId = TenantContext.getRestaurantId();
        return productOptionRepository
                .findByRestaurantIdAndProductId(restaurantId, productId)
                .stream()
                .map(productOptionMapper::toDto)
                .toList();
    }

    /** Get available options for a product. */
    @Transactional(readOnly = true)
    public List<ProductOptionDto> getAvailableOptionsByProduct(String productId) {
        String restaurantId = TenantContext.getRestaurantId();
        return productOptionRepository
                .findByRestaurantIdAndProductIdAndIsAvailable(restaurantId, productId, true)
                .stream()
                .map(productOptionMapper::toDto)
                .toList();
    }

    /** Get a product option by ID. */
    @Transactional(readOnly = true)
    public ProductOptionDto getProductOptionById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        ProductOption option =
                productOptionRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("ProductOption", id));
        return productOptionMapper.toDto(option);
    }

    /** Create a new product option. */
    @Transactional
    public ProductOptionDto createProductOption(CreateProductOptionRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        // Validate product exists
        if (!productRepository.existsByIdAndRestaurantId(request.getProductId(), restaurantId)) {
            throw new NotFoundException("Product", request.getProductId());
        }

        ProductOption option =
                ProductOption.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .name(request.getName())
                        .description(request.getDescription())
                        .priceAdjustment(request.getPriceAdjustment())
                        .productId(request.getProductId())
                        .isDefault(request.isDefault())
                        .isAvailable(request.isAvailable())
                        .build();

        option = productOptionRepository.save(option);
        return productOptionMapper.toDto(option);
    }

    /** Update a product option. */
    @Transactional
    public ProductOptionDto updateProductOption(String id, UpdateProductOptionRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        ProductOption option =
                productOptionRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("ProductOption", id));

        if (request.getName() != null) {
            option.setName(request.getName());
        }

        if (request.getDescription() != null) {
            option.setDescription(request.getDescription());
        }

        if (request.getPriceAdjustment() != null) {
            option.setPriceAdjustment(request.getPriceAdjustment());
        }

        if (request.isDefault() != null) {
            option.setDefault(request.isDefault());
        }

        if (request.isAvailable() != null) {
            option.setAvailable(request.isAvailable());
        }

        option = productOptionRepository.save(option);
        return productOptionMapper.toDto(option);
    }

    /** Delete a product option. */
    @Transactional
    public void deleteProductOption(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        ProductOption option =
                productOptionRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("ProductOption", id));
        productOptionRepository.delete(option);
    }
}
