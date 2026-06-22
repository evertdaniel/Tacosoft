package com.restaurant.app.auth.controller;

import com.restaurant.app.auth.dto.LoginRequest;
import com.restaurant.app.auth.dto.LoginResponse;
import com.restaurant.app.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for authentication endpoints. */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and token management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Login endpoint - authenticate user and return JWT token. POST /auth/login */
    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate user with username/password and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
