package com.restaurant.app.menu.controller;

import com.restaurant.app.menu.dto.CreateProductOptionRequest;
import com.restaurant.app.menu.dto.ProductOptionDto;
import com.restaurant.app.menu.dto.UpdateProductOptionRequest;
import com.restaurant.app.menu.service.ProductOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for product option CRUD endpoints. */
@RestController
@RequestMapping("/product-options")
@Tag(name = "Product Options", description = "Product option management")
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    public ProductOptionController(ProductOptionService productOptionService) {
        this.productOptionService = productOptionService;
    }

    /** Create a new product option. POST /product-options */
    @PostMapping
    @Operation(summary = "Create product option", description = "Create a new product option")
    public ResponseEntity<ProductOptionDto> createProductOption(
            @Valid @RequestBody CreateProductOptionRequest request) {
        ProductOptionDto response = productOptionService.createProductOption(request);
        return ResponseEntity.ok(response);
    }

    /** Get all options for current restaurant. GET /product-options */
    @GetMapping
    @Operation(
            summary = "List product options",
            description = "Get all options for current restaurant")
    public ResponseEntity<List<ProductOptionDto>> getAllProductOptions() {
        List<ProductOptionDto> response = productOptionService.getAllProductOptions();
        return ResponseEntity.ok(response);
    }

    /** Get options by product. GET /product-options/product/{productId} */
    @GetMapping("/product/{productId}")
    @Operation(summary = "List options by product", description = "Get options by product ID")
    public ResponseEntity<List<ProductOptionDto>> getOptionsByProduct(
            @PathVariable String productId) {
        List<ProductOptionDto> response = productOptionService.getOptionsByProduct(productId);
        return ResponseEntity.ok(response);
    }

    /** Get available options for a product. GET /product-options/product/{productId}/available */
    @GetMapping("/product/{productId}/available")
    @Operation(
            summary = "List available options",
            description = "Get available options for a product")
    public ResponseEntity<List<ProductOptionDto>> getAvailableOptionsByProduct(
            @PathVariable String productId) {
        List<ProductOptionDto> response =
                productOptionService.getAvailableOptionsByProduct(productId);
        return ResponseEntity.ok(response);
    }

    /** Get a product option by ID. GET /product-options/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get product option", description = "Get a product option by ID")
    public ResponseEntity<ProductOptionDto> getProductOptionById(@PathVariable String id) {
        ProductOptionDto response = productOptionService.getProductOptionById(id);
        return ResponseEntity.ok(response);
    }

    /** Update a product option. PUT /product-options/{id} */
    @PutMapping("/{id}")
    @Operation(summary = "Update product option", description = "Update a product option by ID")
    public ResponseEntity<ProductOptionDto> updateProductOption(
            @PathVariable String id, @Valid @RequestBody UpdateProductOptionRequest request) {
        ProductOptionDto response = productOptionService.updateProductOption(id, request);
        return ResponseEntity.ok(response);
    }

    /** Delete a product option. DELETE /product-options/{id} */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product option", description = "Delete a product option by ID")
    public ResponseEntity<Void> deleteProductOption(@PathVariable String id) {
        productOptionService.deleteProductOption(id);
        return ResponseEntity.noContent().build();
    }
}
