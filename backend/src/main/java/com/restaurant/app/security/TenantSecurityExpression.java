package com.restaurant.app.security;

import java.util.Arrays;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Custom Spring Security expression for multi-tenant RBAC. Checks that the authenticated user has
 * the required role in the restaurant currently selected by the x-restaurant-id header.
 */
@Component("tenantSecurityExpression")
public class TenantSecurityExpression {

    /** Check if the current user has the given role in the current restaurant. */
    public boolean hasRole(String role) {
        return hasAnyRole(role);
    }

    /** Check if the current user has any of the given roles in the current restaurant. */
    public boolean hasAnyRole(String... roles) {
        String restaurantId = TenantContext.getRestaurantId();
        if (restaurantId == null) {
            return false;
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsAdapter userDetails) {
            return Arrays.stream(roles)
                    .anyMatch(role -> userDetails.hasRoleInRestaurant(restaurantId, role));
        }

        return false;
    }

    /** Check if the current user has all the given roles in the current restaurant. */
    public boolean hasAllRoles(String... roles) {
        String restaurantId = TenantContext.getRestaurantId();
        if (restaurantId == null) {
            return false;
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsAdapter userDetails) {
            return Arrays.stream(roles)
                    .allMatch(role -> userDetails.hasRoleInRestaurant(restaurantId, role));
        }

        return false;
    }
}
