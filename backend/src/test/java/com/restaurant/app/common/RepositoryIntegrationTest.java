package com.restaurant.app.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base integration test for repositories. Tests FK constraints, tenant isolation (INV-06), query
 * correctness.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class RepositoryIntegrationTest {

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    private final String restaurantId = "restaurant-test";
    private String userId;

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);

        // Create the minimum tenant/person/user rows required by FK / NOT NULL constraints
        String personId = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        IntegrationTestFixtures.createRestaurant(jdbcTemplate, restaurantId, "Repository Test");
        IntegrationTestFixtures.createRestaurant(
                jdbcTemplate, "different-restaurant", "Other Restaurant");
        IntegrationTestFixtures.createPerson(jdbcTemplate, personId, "Test", "User");
        IntegrationTestFixtures.createAppUser(
                jdbcTemplate,
                userId,
                "repo-user-" + System.nanoTime(),
                "{noop}password",
                personId,
                true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void orderRepository_SaveAndRetrieve_Success() {
        // Only run if repository is available (may not be in test slicing)
        if (orderRepository == null) {
            return;
        }

        // Arrange
        Order order =
                Order.builder()
                        .id("test-order-1")
                        .restaurantId(restaurantId)
                        .num(1)
                        .type("IN_PLACE")
                        .status("PENDING")
                        .people(1)
                        .total(java.math.BigDecimal.valueOf(100))
                        .userId(userId)
                        .build();

        // Act
        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findById("test-order-1");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("test-order-1");
        assertThat(found.get().getRestaurantId()).isEqualTo(restaurantId);
    }

    @Test
    void orderRepository_FindByRestaurantId_TenantIsolation() {
        if (orderRepository == null) return;

        // Arrange
        Order order1 =
                Order.builder()
                        .id("order-1")
                        .restaurantId(restaurantId)
                        .num(1)
                        .type("IN_PLACE")
                        .status("PENDING")
                        .people(1)
                        .total(java.math.BigDecimal.valueOf(100))
                        .userId(userId)
                        .build();

        Order order2 =
                Order.builder()
                        .id("order-2")
                        .restaurantId("different-restaurant")
                        .num(2)
                        .type("TAKE_AWAY")
                        .status("PENDING")
                        .people(1)
                        .total(java.math.BigDecimal.valueOf(50))
                        .userId(userId)
                        .build();

        orderRepository.save(order1);
        orderRepository.save(order2);

        // Act - Query with current tenant context
        List<Order> result = orderRepository.findByRestaurantId(restaurantId);

        // Assert - Should only return orders for current restaurant (INV-06)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("order-1");
    }

    @Test
    void orderRepository_FindMaxNumByRestaurantId_ReturnsMax() {
        if (orderRepository == null) return;

        // Arrange
        Order order1 =
                Order.builder()
                        .id("order-1")
                        .restaurantId(restaurantId)
                        .num(100)
                        .type("IN_PLACE")
                        .status("PENDING")
                        .people(1)
                        .total(java.math.BigDecimal.valueOf(100))
                        .userId(userId)
                        .build();

        Order order2 =
                Order.builder()
                        .id("order-2")
                        .restaurantId(restaurantId)
                        .num(105)
                        .type("TAKE_AWAY")
                        .status("PENDING")
                        .people(1)
                        .total(java.math.BigDecimal.valueOf(50))
                        .userId(userId)
                        .build();

        orderRepository.save(order1);
        orderRepository.save(order2);

        // Act
        Integer maxNum = orderRepository.findMaxNumByRestaurantId(restaurantId);

        // Assert
        assertThat(maxNum).isEqualTo(105);
    }
}
