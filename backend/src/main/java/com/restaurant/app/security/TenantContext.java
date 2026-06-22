package com.restaurant.app.security;

/**
 * Thread-local context for tenant (restaurant) isolation. Implements INV-06 (tenant isolation) and
 * ADR-004.
 */
public class TenantContext {

    private static final ThreadLocal<String> restaurantId = new ThreadLocal<>();

    /** Set current restaurant ID for this request thread. */
    public static void setRestaurantId(String id) {
        restaurantId.set(id);
    }

    /** Get current restaurant ID for this request thread. */
    public static String getRestaurantId() {
        return restaurantId.get();
    }

    /** Clear tenant context after request completes. */
    public static void clear() {
        restaurantId.remove();
    }
}
