package com.restaurant.app.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.user.dto.UserDto;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link com.restaurant.app.user.mapper.UserMapper}. */
class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toDto_MapsUserWithPrimaryRoleAndRestaurantRoles() {
        Role primaryRole = Role.builder().id(1).name("ADMIN").description("Admin").build();
        Role waiterRole = Role.builder().id(2).name("WAITER").description("Waiter").build();
        AppUser user =
                AppUser.builder()
                        .id("user-1")
                        .username("john")
                        .active(true)
                        .primaryRole(primaryRole)
                        .lastLogin(LocalDateTime.of(2026, 6, 22, 10, 0))
                        .restaurantRoles(
                                List.of(
                                        UserRestaurantRole.builder()
                                                .id("urr-1")
                                                .restaurantId("restaurant-1")
                                                .role(waiterRole)
                                                .build()))
                        .build();

        UserDto dto = mapper.toDto(user);

        assertThat(dto.getId()).isEqualTo("user-1");
        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getPrimaryRole().name()).isEqualTo("ADMIN");
        assertThat(dto.getRestaurantRoles()).hasSize(1);
        assertThat(dto.getRestaurantRoles().get(0).name()).isEqualTo("WAITER");
        assertThat(dto.getLastLogin()).isEqualTo("2026-06-22T10:00");
    }

    @Test
    void toDto_MapsUserWithNullPrimaryRoleAndLastLogin() {
        AppUser user =
                AppUser.builder()
                        .id("user-1")
                        .username("john")
                        .active(false)
                        .restaurantRoles(List.of())
                        .build();

        UserDto dto = mapper.toDto(user);

        assertThat(dto.getPrimaryRole()).isNull();
        assertThat(dto.getLastLogin()).isNull();
        assertThat(dto.isActive()).isFalse();
    }
}
