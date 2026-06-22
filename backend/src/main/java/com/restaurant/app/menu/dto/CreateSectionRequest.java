package com.restaurant.app.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request DTO for creating a section. */
public record CreateSectionRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Display order is required") Integer displayOrder,
        boolean isActive) {}
