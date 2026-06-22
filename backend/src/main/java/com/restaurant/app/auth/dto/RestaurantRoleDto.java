package com.restaurant.app.auth.dto;

/** DTO for user's role in a specific restaurant. */
public record RestaurantRoleDto(String restaurantId, String restaurantName, RoleDto role) {}
