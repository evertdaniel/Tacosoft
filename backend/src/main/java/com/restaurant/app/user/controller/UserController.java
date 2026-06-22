package com.restaurant.app.user.controller;

import com.restaurant.app.user.dto.AssignRoleRequest;
import com.restaurant.app.user.dto.CreateUserRequest;
import com.restaurant.app.user.dto.UpdateUserRequest;
import com.restaurant.app.user.dto.UserDto;
import com.restaurant.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for user management endpoints. */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management and role assignment")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Get all users for current restaurant. GET /users */
    @GetMapping
    @Operation(summary = "List users", description = "Get all users for current restaurant")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /** Get a user by ID. GET /users/{id} */
    @GetMapping("/{id}")
    @Operation(summary = "Get user", description = "Get a user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /** Create a new user. POST /users */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }

    /** Update a user. PUT /users/{id} */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update a user by ID")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String id, @Valid @RequestBody UpdateUserRequest request) {
        UserDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    /** Delete a user. DELETE /users/{id} */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by ID")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /** Assign a restaurant role to a user. POST /users/{id}/roles */
    @PostMapping("/{id}/roles")
    @Operation(summary = "Assign role", description = "Assign a restaurant role to a user")
    public ResponseEntity<Void> assignRestaurantRole(
            @PathVariable String id, @Valid @RequestBody AssignRoleRequest request) {
        userService.assignRestaurantRole(id, request);
        return ResponseEntity.ok().build();
    }

    /** Remove a restaurant role from a user. DELETE /users/{id}/roles/{restaurantId}/{roleId} */
    @DeleteMapping("/{id}/roles/{restaurantId}/{roleId}")
    @Operation(summary = "Remove role", description = "Remove a restaurant role from a user")
    public ResponseEntity<Void> removeRestaurantRole(
            @PathVariable String id,
            @PathVariable String restaurantId,
            @PathVariable Integer roleId) {
        userService.removeRestaurantRole(id, restaurantId, roleId);
        return ResponseEntity.noContent().build();
    }
}
