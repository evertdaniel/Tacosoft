package com.restaurant.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.auth.repository.AppUserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Unit tests for {@link UserDetailsServiceAdapter}. */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceAdapterTest {

    @Mock private AppUserRepository userRepository;

    @InjectMocks private UserDetailsServiceAdapter userDetailsService;

    private final String userId = "user-1";
    private final String username = "john";

    @Test
    void loadUserByUsername_ExistingUser_ReturnsUserDetailsAdapter() {
        AppUser user = userEntity();
        AppUser userWithRoles = userEntityWithRoles();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(userWithRoles));

        UserDetails result = userDetailsService.loadUserByUsername(username);

        assertThat(result).isInstanceOf(UserDetailsAdapter.class);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        UserDetailsAdapter adapter = (UserDetailsAdapter) result;
        assertThat(adapter.getId()).isEqualTo(userId);
        assertThat(adapter.hasAnyRoleInRestaurant("restaurant-1")).isTrue();
        assertThat(adapter.hasRoleInRestaurant("restaurant-1", "ADMIN")).isTrue();
        assertThat(adapter.hasRoleInRestaurant("restaurant-1", "COOK")).isFalse();
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void loadUserByUsername_UserWithRolesNotFound_ThrowsUsernameNotFoundException() {
        AppUser user = userEntity();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    private AppUser userEntity() {
        return AppUser.builder()
                .id(userId)
                .username(username)
                .password("password")
                .active(true)
                .build();
    }

    private AppUser userEntityWithRoles() {
        Role role = Role.builder().id(1).name("ADMIN").description("Admin role").build();
        UserRestaurantRole userRole =
                UserRestaurantRole.builder()
                        .id("urr-1")
                        .userId(userId)
                        .restaurantId("restaurant-1")
                        .role(role)
                        .roleId(role.getId())
                        .build();
        return AppUser.builder()
                .id(userId)
                .username(username)
                .password("password")
                .active(true)
                .primaryRole(role)
                .restaurantRoles(List.of(userRole))
                .build();
    }
}
