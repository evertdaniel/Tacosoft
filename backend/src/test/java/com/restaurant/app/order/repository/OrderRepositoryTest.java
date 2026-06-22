package com.restaurant.app.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.common.IntegrationTestFixtures;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Spring Boot test for {@link OrderRepository} custom query methods. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryTest {

    @Autowired private OrderRepository orderRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    private final String restaurantId = "restaurant-order-test";
    private String userId;

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);

        String personId = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        IntegrationTestFixtures.createRestaurant(jdbcTemplate, restaurantId, "Order Repo Test");
        IntegrationTestFixtures.createPerson(jdbcTemplate, personId, "Test", "User");
        IntegrationTestFixtures.createAppUser(
                jdbcTemplate,
                userId,
                "order-user-" + System.nanoTime(),
                "{noop}pass",
                personId,
                true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void findByRestaurantIdAndStatusNotIn_ExcludesMatchedStatuses() {
        // Arrange
        saveOrder("order-1", 1, "PENDING");
        saveOrder("order-2", 2, "CLOSED");
        saveOrder("order-3", 3, "CANCELLED");

        // Act
        List<Order> result =
                orderRepository.findByRestaurantIdAndStatusNotIn(
                        restaurantId, List.of("CLOSED", "CANCELLED"));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("order-1");
    }

    @Test
    void findActiveOrders_ExcludesClosedAndCancelled() {
        // Arrange
        saveOrder("order-1", 1, "PENDING");
        saveOrder("order-2", 2, "CLOSED");

        // Act
        List<Order> result = orderRepository.findActiveOrders(restaurantId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void findMaxNumByRestaurantId_ReturnsMaximum() {
        // Arrange
        saveOrder("order-1", 10, "PENDING");
        saveOrder("order-2", 25, "PENDING");

        // Act
        Integer maxNum = orderRepository.findMaxNumByRestaurantId(restaurantId);

        // Assert
        assertThat(maxNum).isEqualTo(25);
    }

    @Test
    void findMaxNumByRestaurantId_NoOrders_ReturnsNull() {
        // Act
        Integer maxNum = orderRepository.findMaxNumByRestaurantId(restaurantId);

        // Assert
        assertThat(maxNum).isNull();
    }

    @Test
    void getMaxOrderNum_ReturnsZeroWhenEmpty() {
        // Act
        Integer maxNum = orderRepository.getMaxOrderNum(restaurantId);

        // Assert
        assertThat(maxNum).isEqualTo(0);
    }

    @Test
    void findByRestaurantIdAndDateRange_ReturnsOrdersInRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        saveOrder("order-1", 1, "PENDING");

        // Act
        List<Order> result =
                orderRepository.findByRestaurantIdAndDateRange(
                        restaurantId, now.minusDays(1), now.plusDays(1));

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void findByNumAndRestaurantId_ReturnsMatchingOrder() {
        // Arrange
        saveOrder("order-1", 42, "PENDING");

        // Act
        Optional<Order> result = orderRepository.findByNumAndRestaurantId(42, restaurantId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("order-1");
    }

    private void saveOrder(String id, int num, String status) {
        Order order =
                Order.builder()
                        .id(id)
                        .restaurantId(restaurantId)
                        .num(num)
                        .type("IN_PLACE")
                        .status(status)
                        .people(1)
                        .total(BigDecimal.valueOf(100))
                        .userId(userId)
                        .build();
        orderRepository.save(order);
    }
}
