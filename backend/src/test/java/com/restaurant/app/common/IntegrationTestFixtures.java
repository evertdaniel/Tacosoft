package com.restaurant.app.common;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Shared SQL-based fixtures for integration/invariant tests.
 *
 * <p>Creates the minimum tenant, person and user rows required by the NOT NULL / FK constraints
 * without touching production code. All methods use {@link JdbcTemplate} so they work in both
 * transactional and non-transactional tests.
 */
public final class IntegrationTestFixtures {

    private IntegrationTestFixtures() {}

    public static final int ADMIN_ROLE_ID = 1;

    public static String createRestaurant(JdbcTemplate jdbc, String restaurantId, String name) {
        jdbc.update(
                "INSERT INTO restaurant (id, name, currency) VALUES (?, ?, 'MXN')",
                restaurantId,
                name);
        return restaurantId;
    }

    public static String createSection(
            JdbcTemplate jdbc, String sectionId, String restaurantId, String name) {
        jdbc.update(
                "INSERT INTO section (id, restaurant_id, name) VALUES (?, ?, ?)",
                sectionId,
                restaurantId,
                name);
        return sectionId;
    }

    public static String createCategory(
            JdbcTemplate jdbc,
            String categoryId,
            String restaurantId,
            String sectionId,
            String name) {
        jdbc.update(
                "INSERT INTO category (id, restaurant_id, section_id, name) VALUES (?, ?, ?, ?)",
                categoryId,
                restaurantId,
                sectionId,
                name);
        return categoryId;
    }

    public static String createProduct(
            JdbcTemplate jdbc,
            String productId,
            String restaurantId,
            String categoryId,
            String name,
            java.math.BigDecimal price) {
        jdbc.update(
                "INSERT INTO product (id, restaurant_id, category_id, name, price, status) VALUES"
                        + " (?, ?, ?, ?, ?, 'AVAILABLE')",
                productId,
                restaurantId,
                categoryId,
                name,
                price);
        return productId;
    }

    public static String createPerson(
            JdbcTemplate jdbc, String personId, String firstName, String lastName) {
        jdbc.update(
                "INSERT INTO person (id, first_name, last_name) VALUES (?, ?, ?)",
                personId,
                firstName,
                lastName);
        return personId;
    }

    public static String createAppUser(
            JdbcTemplate jdbc,
            String userId,
            String username,
            String password,
            String personId,
            boolean active) {
        jdbc.update(
                "INSERT INTO app_user (id, username, password, person_id, is_active) VALUES (?, ?,"
                        + " ?, ?, ?)",
                userId,
                username,
                password,
                personId,
                active);
        return userId;
    }

    public static void assignRole(
            JdbcTemplate jdbc, String userId, String restaurantId, int roleId) {
        jdbc.update(
                "INSERT INTO user_restaurant_role (id, user_id, restaurant_id, role_id) VALUES (?,"
                        + " ?, ?, ?)",
                UUID.randomUUID().toString(),
                userId,
                restaurantId,
                roleId);
    }

    public static void cleanupUserAndRestaurant(
            JdbcTemplate jdbc, String userId, String personId, String restaurantId) {
        if (userId != null) {
            jdbc.update("DELETE FROM `order` WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM user_restaurant_role WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM app_user WHERE id = ?", userId);
        }
        if (restaurantId != null) {
            // Menu data is scoped to the restaurant; remove it before the restaurant row.
            jdbc.update(
                    "DELETE FROM product_option WHERE product_id IN (SELECT id FROM product WHERE"
                            + " restaurant_id = ?)",
                    restaurantId);
            jdbc.update("DELETE FROM product WHERE restaurant_id = ?", restaurantId);
            jdbc.update("DELETE FROM category WHERE restaurant_id = ?", restaurantId);
            jdbc.update("DELETE FROM section WHERE restaurant_id = ?", restaurantId);
        }
        if (personId != null) {
            jdbc.update("DELETE FROM person WHERE id = ?", personId);
        }
        if (restaurantId != null) {
            jdbc.update("DELETE FROM restaurant WHERE id = ?", restaurantId);
        }
    }
}
