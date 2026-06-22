package com.restaurant.app.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Request DTO for login. */
public record LoginRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password) {}
