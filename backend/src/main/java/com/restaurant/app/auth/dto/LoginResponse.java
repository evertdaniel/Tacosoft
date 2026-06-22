package com.restaurant.app.auth.dto;

/** Response DTO for successful login. */
public record LoginResponse(String token, UserDto user, RestaurantInfoDto currentRestaurant) {}
