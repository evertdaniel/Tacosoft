package com.restaurant.app.auth.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.auth.dto.RoleDto;
import com.restaurant.app.auth.dto.UserDto;
import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.user.model.Person;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link RoleMapper} and {@link UserMapper}. */
class AuthMapperTest {

    private final RoleMapper roleMapper = new RoleMapperImpl();
    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void roleMapper_MapsRoleToDto() {
        Role role = Role.builder().id(1).name("ADMIN").description("Admin role").build();

        RoleDto dto = roleMapper.toDto(role);

        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.name()).isEqualTo("ADMIN");
        assertThat(dto.description()).isEqualTo("Admin role");
    }

    @Test
    void userMapper_MapsUserWithPersonAndRolesToDto() {
        Person person =
                Person.builder()
                        .id("person-1")
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@example.com")
                        .build();
        Role primaryRole = Role.builder().id(1).name("ADMIN").description("Admin").build();
        Role waiterRole = Role.builder().id(2).name("WAITER").description("Waiter").build();
        AppUser user =
                AppUser.builder()
                        .id("user-1")
                        .username("john")
                        .active(true)
                        .primaryRole(primaryRole)
                        .person(person)
                        .restaurantRoles(
                                List.of(
                                        UserRestaurantRole.builder()
                                                .id("urr-1")
                                                .restaurantId("restaurant-1")
                                                .role(waiterRole)
                                                .build()))
                        .build();

        UserDto dto = userMapper.toDto(user);

        assertThat(dto.id()).isEqualTo("user-1");
        assertThat(dto.username()).isEqualTo("john");
        assertThat(dto.active()).isTrue();
        assertThat(dto.firstName()).isEqualTo("John");
        assertThat(dto.lastName()).isEqualTo("Doe");
        assertThat(dto.email()).isEqualTo("john@example.com");
        assertThat(dto.primaryRole().name()).isEqualTo("ADMIN");
        assertThat(dto.restaurantRoles()).hasSize(1);
        assertThat(dto.restaurantRoles().get(0).restaurantId()).isEqualTo("restaurant-1");
        assertThat(dto.restaurantRoles().get(0).role().name()).isEqualTo("WAITER");
    }

    @Test
    void userMapper_MapsUserWithNullPersonAndRolesToDto() {
        AppUser user =
                AppUser.builder()
                        .id("user-1")
                        .username("john")
                        .active(true)
                        .restaurantRoles(null)
                        .build();

        UserDto dto = userMapper.toDto(user);

        assertThat(dto.id()).isEqualTo("user-1");
        assertThat(dto.firstName()).isNull();
        assertThat(dto.restaurantRoles()).isEmpty();
        assertThat(dto.primaryRole()).isNull();
    }
}
