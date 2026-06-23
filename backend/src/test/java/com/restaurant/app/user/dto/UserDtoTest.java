package com.restaurant.app.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.auth.dto.RoleDto;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for user DTOs. */
class UserDtoTest {

    @Test
    void userDto_GettersAndSetters() {
        UserDto dto = new UserDto();
        dto.setId("user-1");
        dto.setUsername("john");
        dto.setActive(true);
        dto.setPrimaryRole(new RoleDto(1, "ADMIN", "Admin"));
        dto.setRestaurantRoles(List.of(new RoleDto(2, "WAITER", "Waiter")));
        dto.setLastLogin("2026-06-22T10:00");

        assertThat(dto.getId()).isEqualTo("user-1");
        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getPrimaryRole().name()).isEqualTo("ADMIN");
        assertThat(dto.getRestaurantRoles()).hasSize(1);
        assertThat(dto.getLastLogin()).isEqualTo("2026-06-22T10:00");
    }

    @Test
    void createUserRequest_GettersAndSetters() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john");
        request.setPassword("secret123");
        request.setPrimaryRoleId(1);
        request.setPersonId("person-1");

        assertThat(request.getUsername()).isEqualTo("john");
        assertThat(request.getPassword()).isEqualTo("secret123");
        assertThat(request.getPrimaryRoleId()).isEqualTo(1);
        assertThat(request.getPersonId()).isEqualTo("person-1");
    }

    @Test
    void updateUserRequest_GettersAndSetters() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("newsecret");
        request.setPrimaryRoleId(2);
        request.setActive(false);
        request.setPersonId("person-1");

        assertThat(request.getPassword()).isEqualTo("newsecret");
        assertThat(request.getPrimaryRoleId()).isEqualTo(2);
        assertThat(request.getActive()).isFalse();
        assertThat(request.getPersonId()).isEqualTo("person-1");
    }

    @Test
    void assignRoleRequest_GettersAndSetters() {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setRestaurantId("restaurant-1");
        request.setRoleId(2);

        assertThat(request.getRestaurantId()).isEqualTo("restaurant-1");
        assertThat(request.getRoleId()).isEqualTo(2);
    }
}
