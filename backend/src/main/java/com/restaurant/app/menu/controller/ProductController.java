package com.restaurant.app.menu.controller;

import com.restaurant.app.menu.dto.CreateProductRequest;
import com.restaurant.app.menu.dto.ProductDto;
import com.restaurant.app.menu.dto.UpdateProductRequest;
import com.restaurant.app.menu.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Controller for product CRUD endpoints. */
@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Menu product management")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** Create a new product. POST /products */
    @PostMapping
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Create product", description = "Create a new menu product")
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductDto response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(response.getId())
                                .toUri())
                .body(response);
    }

    /** Get all products for current restaurant. GET /products */
    @GetMapping
    @Operation(summary = "List products", description = "Get all products for current restaurant")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    /** Get active products only. GET /products/active */
    @GetMapping("/active")
    @Operation(summary = "List active products", description = "Get active products only")
    public ResponseEntity<List<ProductDto>> getActiveProducts() {
        List<ProductDto> response = productService.getActiveProducts();
        return ResponseEntity.ok(response);
    }

    /** Get available products (active and in stock). GET /products/available */
    @GetMapping("/available")
    @Operation(
            summary = "List available products",
            description = "Get available products (active and in stock)")
    public ResponseEntity<List<ProductDto>> getAvailableProducts() {
        List<ProductDto> response = productService.getAvailableProducts();
        return ResponseEntity.ok(response);
    }

    /** Get products by category. GET /products/category/{categoryId} */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List products by category", description = "Get products by category ID")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String categoryId) {
        List<ProductDto> response = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    /** Get a product by ID. GET /products/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get product", description = "Get a product by ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        ProductDto response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    /** Update a product. PUT /products/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Update product", description = "Update a product by ID")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable String id, @Valid @RequestBody UpdateProductRequest request) {
        ProductDto response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /** Delete a product. DELETE /products/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Delete a product by ID")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
