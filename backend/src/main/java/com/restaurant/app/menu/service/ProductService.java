package com.restaurant.app.menu.service;

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
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for product operations. */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductionAreaRepository productionAreaRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductionAreaRepository productionAreaRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productionAreaRepository = productionAreaRepository;
        this.productMapper = productMapper;
    }

    /** Get all products for current restaurant. */
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        String restaurantId = TenantContext.getRestaurantId();
        return productRepository.findByRestaurantId(restaurantId).stream()
                .map(productMapper::toDto)
                .toList();
    }

    /** Get active products only. */
    @Transactional(readOnly = true)
    public List<ProductDto> getActiveProducts() {
        String restaurantId = TenantContext.getRestaurantId();
        return productRepository.findByRestaurantIdAndIsActive(restaurantId, true).stream()
                .map(productMapper::toDto)
                .toList();
    }

    /** Get available products (active and in stock). */
    @Transactional(readOnly = true)
    public List<ProductDto> getAvailableProducts() {
        String restaurantId = TenantContext.getRestaurantId();
        return productRepository
                .findByRestaurantIdAndIsActiveAndStatus(restaurantId, true, "AVAILABLE")
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    /** Get products by category. */
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(String categoryId) {
        String restaurantId = TenantContext.getRestaurantId();
        return productRepository.findByRestaurantIdAndCategoryId(restaurantId, categoryId).stream()
                .map(productMapper::toDto)
                .toList();
    }

    /** Get a product by ID. */
    @Transactional(readOnly = true)
    public ProductDto getProductById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Product product =
                productRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Product", id));
        return productMapper.toDto(product);
    }

    /** Create a new product. */
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        // Validate category exists
        if (!categoryRepository.existsByIdAndRestaurantId(request.getCategoryId(), restaurantId)) {
            throw new NotFoundException("Category", request.getCategoryId());
        }

        // Validate production area if provided
        if (request.getProductionAreaId() != null
                && !productionAreaRepository.existsByIdAndRestaurantId(
                        request.getProductionAreaId(), restaurantId)) {
            throw new NotFoundException("ProductionArea", request.getProductionAreaId());
        }

        // Validate stock if manageStock is enabled
        if (request.isManageStock() && request.getStock() <= 0) {
            throw new ConflictException(
                    "Product with stock management must have initial stock > 0");
        }

        Product product =
                Product.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .name(request.getName())
                        .description(request.getDescription())
                        .price(request.getPrice())
                        .categoryId(request.getCategoryId())
                        .taxRate(request.getTaxRate())
                        .stock(request.getStock())
                        .manageStock(request.isManageStock())
                        .status(request.getStatus())
                        .imageUrl(request.getImageUrl())
                        .preparationTime(request.getPreparationTime())
                        .isActive(request.isActive())
                        .productionAreaId(request.getProductionAreaId())
                        .build();

        product = productRepository.save(product);
        return productMapper.toDto(product);
    }

    /** Update a product. */
    @Transactional
    public ProductDto updateProduct(String id, UpdateProductRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        Product product =
                productRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Product", id));

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getTaxRate() != null) {
            product.setTaxRate(request.getTaxRate());
        }

        if (request.getStock() != null) {
            if (product.isManageStock() && request.getStock() < 0) {
                throw new ConflictException(
                        "Stock cannot be negative for product with stock management");
            }
            product.setStock(request.getStock());
        }

        if (request.isManageStock() != null) {
            product.setManageStock(request.isManageStock());
        }

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        if (request.getPreparationTime() != null) {
            product.setPreparationTime(request.getPreparationTime());
        }

        if (request.isActive() != null) {
            product.setActive(request.isActive());
        }

        if (request.getProductionAreaId() != null) {
            if (!productionAreaRepository.existsByIdAndRestaurantId(
                    request.getProductionAreaId(), restaurantId)) {
                throw new NotFoundException("ProductionArea", request.getProductionAreaId());
            }
            product.setProductionAreaId(request.getProductionAreaId());
        }

        product = productRepository.save(product);
        return productMapper.toDto(product);
    }

    /** Delete a product. */
    @Transactional
    public void deleteProduct(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Product product =
                productRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Product", id));
        productRepository.delete(product);
    }

    /** Update product stock (used when orders are placed). */
    @Transactional
    public void updateStock(String productId, int quantityDelta) {
        String restaurantId = TenantContext.getRestaurantId();
        Product product =
                productRepository
                        .findByIdAndRestaurantId(productId, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Product", productId));

        if (!product.isManageStock()) {
            return; // Stock not managed for this product
        }

        int newStock = product.getStock() + quantityDelta;
        if (newStock < 0) {
            throw new ConflictException("Insufficient stock for product: " + product.getName());
        }

        product.setStock(newStock);

        // Auto-update status if out of stock
        if (newStock == 0 && "AVAILABLE".equals(product.getStatus())) {
            product.setStatus("OUT_OF_STOCK");
        }

        productRepository.save(product);
    }
}
