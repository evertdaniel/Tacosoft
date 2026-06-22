package com.restaurant.app.security;

import com.restaurant.app.common.ForbiddenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Multi-tenancy filter. Validates x-restaurant-id header against JWT restaurantRoles. Implements
 * INV-06 (tenant isolation) and ADR-004.
 */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String restaurantId = request.getHeader("x-restaurant-id");

        // Public endpoints bypass tenant validation
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (restaurantId == null) {
            // Security filter chain will handle authentication failure
            filterChain.doFilter(request, response);
            return;
        }

        // Validate that user has role in this restaurant
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsAdapter userDetails) {
                if (!userDetails.hasAnyRoleInRestaurant(restaurantId)) {
                    throw new ForbiddenException(
                            "User does not have access to restaurant: " + restaurantId);
                }
            }
        }

        // Set tenant context for repositories
        TenantContext.setRestaurantId(restaurantId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/auth/login")
                || path.equals("/auth/register")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
