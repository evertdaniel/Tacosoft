package com.restaurant.app.user.mapper;

import com.restaurant.app.auth.dto.RoleDto;
import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.user.dto.UserDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Mapper for User entity and DTOs. */
@Component
public class UserMapper {

    public UserDto toDto(AppUser user) {
        List<RoleDto> roles =
                user.getRestaurantRoles().stream()
                        .map(this::toRoleDto)
                        .collect(Collectors.toList());

        RoleDto primaryRole =
                user.getPrimaryRole() != null ? toRoleDto(user.getPrimaryRole()) : null;

        String lastLogin = user.getLastLogin() != null ? user.getLastLogin().toString() : null;

        return new UserDto(
                user.getId(), user.getUsername(), user.isActive(), primaryRole, roles, lastLogin);
    }

    private RoleDto toRoleDto(com.restaurant.app.auth.model.Role role) {
        return new RoleDto(role.getId(), role.getName(), role.getDescription());
    }

    private RoleDto toRoleDto(UserRestaurantRole userRole) {
        return new RoleDto(
                userRole.getRole().getId(),
                userRole.getRole().getName(),
                userRole.getRole().getDescription());
    }
}
