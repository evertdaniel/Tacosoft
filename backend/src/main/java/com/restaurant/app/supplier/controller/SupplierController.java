package com.restaurant.app.supplier.controller;

import com.restaurant.app.supplier.dto.CreateSupplierRequest;
import com.restaurant.app.supplier.dto.SupplierDto;
import com.restaurant.app.supplier.dto.UpdateSupplierRequest;
import com.restaurant.app.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for supplier CRUD endpoints. Implements T10.1. */
@RestController
@RequestMapping("/suppliers")
@Tag(name = "Suppliers", description = "Supplier management")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /** Create a new supplier. POST /suppliers */
    @PostMapping
    @Operation(summary = "Create supplier", description = "Create a new supplier")
    public ResponseEntity<SupplierDto> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierDto response = supplierService.createSupplier(request);
        return ResponseEntity.ok(response);
    }

    /** Get all suppliers for current restaurant. GET /suppliers */
    @GetMapping
    @Operation(summary = "List suppliers", description = "Get all suppliers for current restaurant")
    public ResponseEntity<List<SupplierDto>> getAllSuppliers() {
        List<SupplierDto> response = supplierService.getAllSuppliers();
        return ResponseEntity.ok(response);
    }

    /** Get active suppliers only. GET /suppliers/active */
    @GetMapping("/active")
    @Operation(summary = "List active suppliers", description = "Get active suppliers only")
    public ResponseEntity<List<SupplierDto>> getActiveSuppliers() {
        List<SupplierDto> response = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(response);
    }

    /** Search suppliers by name. GET /suppliers/search */
    @GetMapping("/search")
    @Operation(summary = "Search suppliers", description = "Search suppliers by name")
    public ResponseEntity<List<SupplierDto>> searchSuppliersByName(@RequestParam String name) {
        List<SupplierDto> response = supplierService.searchSuppliersByName(name);
        return ResponseEntity.ok(response);
    }

    /** Get a supplier by ID. GET /suppliers/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get supplier", description = "Get a supplier by ID")
    public ResponseEntity<SupplierDto> getSupplierById(@PathVariable String id) {
        SupplierDto response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(response);
    }

    /** Update a supplier. PUT /suppliers/{id} */
    @PutMapping("/{id}")
    @Operation(summary = "Update supplier", description = "Update a supplier by ID")
    public ResponseEntity<SupplierDto> updateSupplier(
            @PathVariable String id, @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierDto response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(response);
    }

    /** Delete a supplier. DELETE /suppliers/{id} */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier", description = "Delete a supplier by ID")
    public ResponseEntity<Void> deleteSupplier(@PathVariable String id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
