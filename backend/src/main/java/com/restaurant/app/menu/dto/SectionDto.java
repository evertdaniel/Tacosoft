package com.restaurant.app.menu.dto;

/** DTO for Section. */
public record SectionDto(
        String id,
        String restaurantId,
        String name,
        String description,
        Integer displayOrder,
        boolean isActive) {}
