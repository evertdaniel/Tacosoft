package com.restaurant.app.user.dto;

import com.restaurant.app.auth.dto.RoleDto;
import java.util.List;

/** DTO for user responses. */
public class UserDto {
    private String id;
    private String username;
    private boolean active;
    private RoleDto primaryRole;
    private List<RoleDto> restaurantRoles;
    private String lastLogin;

    public UserDto() {}

    public UserDto(
            String id,
            String username,
            boolean active,
            RoleDto primaryRole,
            List<RoleDto> restaurantRoles,
            String lastLogin) {
        this.id = id;
        this.username = username;
        this.active = active;
        this.primaryRole = primaryRole;
        this.restaurantRoles = restaurantRoles;
        this.lastLogin = lastLogin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public RoleDto getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(RoleDto primaryRole) {
        this.primaryRole = primaryRole;
    }

    public List<RoleDto> getRestaurantRoles() {
        return restaurantRoles;
    }

    public void setRestaurantRoles(List<RoleDto> restaurantRoles) {
        this.restaurantRoles = restaurantRoles;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
}
