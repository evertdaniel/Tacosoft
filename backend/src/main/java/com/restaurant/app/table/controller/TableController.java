package com.restaurant.app.table.controller;

import com.restaurant.app.table.dto.CreateTableRequest;
import com.restaurant.app.table.dto.TableDto;
import com.restaurant.app.table.dto.UpdateTableRequest;
import com.restaurant.app.table.dto.UpdateTableStatusRequest;
import com.restaurant.app.table.service.TableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Controller for table CRUD and status transition endpoints. */
@RestController
@RequestMapping("/tables")
@Tag(name = "Tables", description = "Restaurant table management")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    /** Create a new table. POST /tables */
    @PostMapping
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Create table", description = "Create a new restaurant table")
    public ResponseEntity<TableDto> createTable(@Valid @RequestBody CreateTableRequest request) {
        TableDto response = tableService.createTable(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(response.getId())
                                .toUri())
                .body(response);
    }

    /** Get all tables for current restaurant. GET /tables */
    @GetMapping
    @Operation(summary = "List tables", description = "Get all tables for current restaurant")
    public ResponseEntity<List<TableDto>> getAllTables() {
        List<TableDto> response = tableService.getAllTables();
        return ResponseEntity.ok(response);
    }

    /** Get active tables only. GET /tables/active */
    @GetMapping("/active")
    @Operation(summary = "List active tables", description = "Get active tables only")
    public ResponseEntity<List<TableDto>> getActiveTables() {
        List<TableDto> response = tableService.getActiveTables();
        return ResponseEntity.ok(response);
    }

    /** Get available tables. GET /tables/available */
    @GetMapping("/available")
    @Operation(summary = "List available tables", description = "Get available tables")
    public ResponseEntity<List<TableDto>> getAvailableTables() {
        List<TableDto> response = tableService.getAvailableTables();
        return ResponseEntity.ok(response);
    }

    /** Get a table by ID. GET /tables/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get table", description = "Get a table by ID")
    public ResponseEntity<TableDto> getTableById(@PathVariable String id) {
        TableDto response = tableService.getTableById(id);
        return ResponseEntity.ok(response);
    }

    /** Update a table. PUT /tables/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Update table", description = "Update a table by ID")
    public ResponseEntity<TableDto> updateTable(
            @PathVariable String id, @Valid @RequestBody UpdateTableRequest request) {
        TableDto response = tableService.updateTable(id, request);
        return ResponseEntity.ok(response);
    }

    /** Update table status. PUT /tables/{id}/status */
    @PutMapping("/{id}/status")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('WAITER', 'ADMIN')")
    @Operation(
            summary = "Update table status",
            description = "Update table status with transition validation")
    public ResponseEntity<TableDto> updateTableStatus(
            @PathVariable String id, @Valid @RequestBody UpdateTableStatusRequest request) {
        TableDto response = tableService.updateTableStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /** Delete a table. DELETE /tables/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Delete table", description = "Delete a table by ID")
    public ResponseEntity<Void> deleteTable(@PathVariable String id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
