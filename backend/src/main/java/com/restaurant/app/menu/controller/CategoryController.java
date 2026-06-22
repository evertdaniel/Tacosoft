package com.restaurant.app.menu.controller;

import com.restaurant.app.menu.dto.CategoryDto;
import com.restaurant.app.menu.dto.CreateCategoryRequest;
import com.restaurant.app.menu.dto.UpdateCategoryRequest;
import com.restaurant.app.menu.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for category CRUD endpoints. */
@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Menu category management")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** Create a new category. POST /categories */
    @PostMapping
    @Operation(summary = "Create category", description = "Create a new menu category")
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryDto response = categoryService.createCategory(request);
        return ResponseEntity.ok(response);
    }

    /** Get all categories for current restaurant. GET /categories */
    @GetMapping
    @Operation(
            summary = "List categories",
            description = "Get all categories for current restaurant")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> response = categoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    /** Get active categories only. GET /categories/active */
    @GetMapping("/active")
    @Operation(summary = "List active categories", description = "Get active categories only")
    public ResponseEntity<List<CategoryDto>> getActiveCategories() {
        List<CategoryDto> response = categoryService.getActiveCategories();
        return ResponseEntity.ok(response);
    }

    /** Get a category by ID. GET /categories/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get category", description = "Get a category by ID")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable String id) {
        CategoryDto response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    /** Update a category. PUT /categories/{id} */
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update a category by ID")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable String id, @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryDto response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    /** Delete a category. DELETE /categories/{id} */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category by ID")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
