package com.restaurant.app.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO for assigning a restaurant role to a user. */
public class AssignRoleRequest {
    @NotBlank(message = "Restaurant ID is required")
    private String restaurantId;

    @NotNull(message = "Role ID is required") private Integer roleId;

    public AssignRoleRequest() {}

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
}
