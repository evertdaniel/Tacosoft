package com.restaurant.app.menu.dto;

import jakarta.validation.constraints.NotNull;

/** Request DTO for updating a section. */
public record UpdateSectionRequest(
        String name,
        String description,
        @NotNull(message = "Display order is required") Integer displayOrder,
        Boolean isActive) {}
