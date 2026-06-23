package com.restaurant.app.menu.controller;

import com.restaurant.app.menu.dto.CreateSectionRequest;
import com.restaurant.app.menu.dto.SectionDto;
import com.restaurant.app.menu.dto.UpdateSectionRequest;
import com.restaurant.app.menu.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Controller for Section CRUD endpoints. */
@RestController
@RequestMapping("/sections")
@Tag(name = "Sections", description = "Menu section management")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    /** Create a new section. POST /sections */
    @PostMapping
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Create section", description = "Create a new menu section")
    public ResponseEntity<SectionDto> createSection(
            @Valid @RequestBody CreateSectionRequest request) {
        SectionDto response = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(response.id())
                                .toUri())
                .body(response);
    }

    /** Get all sections for current restaurant. GET /sections */
    @GetMapping
    @Operation(summary = "List sections", description = "Get all sections for current restaurant")
    public ResponseEntity<List<SectionDto>> getAllSections() {
        List<SectionDto> response = sectionService.getAllSections();
        return ResponseEntity.ok(response);
    }

    /** Get active sections only. GET /sections/active */
    @GetMapping("/active")
    @Operation(summary = "List active sections", description = "Get active sections only")
    public ResponseEntity<List<SectionDto>> getActiveSections() {
        List<SectionDto> response = sectionService.getActiveSections();
        return ResponseEntity.ok(response);
    }

    /** Get a section by ID. GET /sections/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get section", description = "Get a section by ID")
    public ResponseEntity<SectionDto> getSectionById(@PathVariable String id) {
        SectionDto response = sectionService.getSectionById(id);
        return ResponseEntity.ok(response);
    }

    /** Update a section. PUT /sections/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Update section", description = "Update a section by ID")
    public ResponseEntity<SectionDto> updateSection(
            @PathVariable String id, @Valid @RequestBody UpdateSectionRequest request) {
        SectionDto response = sectionService.updateSection(id, request);
        return ResponseEntity.ok(response);
    }

    /** Delete a section. DELETE /sections/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN')")
    @Operation(summary = "Delete section", description = "Delete a section by ID")
    public ResponseEntity<Void> deleteSection(@PathVariable String id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}
