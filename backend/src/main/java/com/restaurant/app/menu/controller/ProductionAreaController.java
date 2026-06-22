package com.restaurant.app.menu.controller;

import com.restaurant.app.menu.dto.CreateProductionAreaRequest;
import com.restaurant.app.menu.dto.ProductionAreaDto;
import com.restaurant.app.menu.dto.UpdateProductionAreaRequest;
import com.restaurant.app.menu.service.ProductionAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for production area CRUD endpoints. */
@RestController
@RequestMapping("/production-areas")
@Tag(name = "Production Areas", description = "Kitchen/Bar production area management")
public class ProductionAreaController {

    private final ProductionAreaService productionAreaService;

    public ProductionAreaController(ProductionAreaService productionAreaService) {
        this.productionAreaService = productionAreaService;
    }

    /** Create a new production area. POST /production-areas */
    @PostMapping
    @Operation(summary = "Create production area", description = "Create a new production area")
    public ResponseEntity<ProductionAreaDto> createProductionArea(
            @Valid @RequestBody CreateProductionAreaRequest request) {
        ProductionAreaDto response = productionAreaService.createProductionArea(request);
        return ResponseEntity.ok(response);
    }

    /** Get all production areas for current restaurant. GET /production-areas */
    @GetMapping
    @Operation(
            summary = "List production areas",
            description = "Get all production areas for current restaurant")
    public ResponseEntity<List<ProductionAreaDto>> getAllProductionAreas() {
        List<ProductionAreaDto> response = productionAreaService.getAllProductionAreas();
        return ResponseEntity.ok(response);
    }

    /** Get a production area by ID. GET /production-areas/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get production area", description = "Get a production area by ID")
    public ResponseEntity<ProductionAreaDto> getProductionAreaById(@PathVariable String id) {
        ProductionAreaDto response = productionAreaService.getProductionAreaById(id);
        return ResponseEntity.ok(response);
    }

    /** Update a production area. PUT /production-areas/{id} */
    @PutMapping("/{id}")
    @Operation(summary = "Update production area", description = "Update a production area by ID")
    public ResponseEntity<ProductionAreaDto> updateProductionArea(
            @PathVariable String id, @Valid @RequestBody UpdateProductionAreaRequest request) {
        ProductionAreaDto response = productionAreaService.updateProductionArea(id, request);
        return ResponseEntity.ok(response);
    }

    /** Delete a production area. DELETE /production-areas/{id} */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete production area", description = "Delete a production area by ID")
    public ResponseEntity<Void> deleteProductionArea(@PathVariable String id) {
        productionAreaService.deleteProductionArea(id);
        return ResponseEntity.noContent().build();
    }
}
