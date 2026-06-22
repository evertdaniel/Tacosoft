package com.restaurant.app.auth.dto;

import java.util.List;

/** DTO for user information. */
public record UserDto(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        boolean active,
        RoleDto primaryRole,
        List<RestaurantRoleDto> restaurantRoles) {}
