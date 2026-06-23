package com.restaurant.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Unit tests for TenantSecurityExpression. Verifies per-restaurant RBAC expression logic. */
@ExtendWith(MockitoExtension.class)
class TenantSecurityExpressionTest {

    private TenantSecurityExpression expression;

    @Mock private Authentication authentication;

    private final String restaurantId = "restaurant-1";

    @BeforeEach
    void setUp() {
        expression = new TenantSecurityExpression();
        TenantContext.setRestaurantId(restaurantId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasRole_UserHasRoleInRestaurant_ReturnsTrue() {
        // Arrange
        UserDetailsAdapter user = createUserWithRoles("ADMIN");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        boolean result = expression.hasRole("ADMIN");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void hasAnyRole_UserHasOneMatchingRole_ReturnsTrue() {
        // Arrange
        UserDetailsAdapter user = createUserWithRoles("WAITER");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        boolean result = expression.hasAnyRole("ADMIN", "WAITER", "COOK");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void hasAllRoles_UserMissingOneRole_ReturnsFalse() {
        // Arrange
        UserDetailsAdapter user = createUserWithRoles("WAITER");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        boolean result = expression.hasAllRoles("ADMIN", "WAITER");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_NoTenantContext_ReturnsFalse() {
        // Arrange
        TenantContext.clear();

        // Act
        boolean result = expression.hasAnyRole("ADMIN");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_NotAuthenticated_ReturnsFalse() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        boolean result = expression.hasAnyRole("ADMIN");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_NullAuthentication_ReturnsFalse() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        boolean result = expression.hasAnyRole("ADMIN");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_NonUserDetailsPrincipal_ReturnsFalse() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymous");

        // Act
        boolean result = expression.hasAnyRole("ADMIN");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasRole_RoleInDifferentRestaurant_ReturnsFalse() {
        // Arrange
        UserDetailsAdapter user = createUserWithRolesInRestaurant("other-restaurant", "ADMIN");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        boolean result = expression.hasRole("ADMIN");

        // Assert
        assertThat(result).isFalse();
    }

    // Helper methods

    private UserDetailsAdapter createUserWithRoles(String... roleNames) {
        return createUserWithRolesInRestaurant(restaurantId, roleNames);
    }

    private UserDetailsAdapter createUserWithRolesInRestaurant(
            String restaurantId, String... roleNames) {
        Role primaryRole = new Role();
        primaryRole.setId(1);
        primaryRole.setName(roleNames[0]);

        List<UserRestaurantRole> restaurantRoles =
                java.util.Arrays.stream(roleNames)
                        .map(
                                name -> {
                                    Role role = new Role();
                                    role.setId(1);
                                    role.setName(name);
                                    UserRestaurantRole urr = new UserRestaurantRole();
                                    urr.setRestaurantId(restaurantId);
                                    urr.setRole(role);
                                    return urr;
                                })
                        .toList();

        return new UserDetailsAdapter(
                "user-1", "testuser", "password", true, restaurantRoles, primaryRole);
    }
}
