package com.restaurant.app.billing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.billing.model.FolioSequence;
import com.restaurant.app.billing.model.Invoice;
import com.restaurant.app.common.IntegrationTestFixtures;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
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

/** Spring Boot test for {@link InvoiceRepository} custom query methods. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceRepositoryTest {

    @Autowired private InvoiceRepository invoiceRepository;

    @Autowired private FolioSequenceRepository folioSequenceRepository;

    @Autowired private OrderRepository orderRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    private final String restaurantId = "restaurant-invoice-test";
    private String userId;
    private int orderSequence = 0;

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
        IntegrationTestFixtures.createRestaurant(jdbcTemplate, restaurantId, "Invoice Repo Test");

        String personId = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        IntegrationTestFixtures.createPerson(jdbcTemplate, personId, "Test", "User");
        IntegrationTestFixtures.createAppUser(
                jdbcTemplate,
                userId,
                "invoice-user-" + System.nanoTime(),
                "{noop}pass",
                personId,
                true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void findByOrderIdAndRestaurantId_ReturnsInvoice() {
        // Arrange
        String orderId = createOrder("order-1");
        saveInvoice("invoice-1", orderId, 1L, false);

        // Act
        Optional<Invoice> result =
                invoiceRepository.findByOrderIdAndRestaurantId(orderId, restaurantId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getFolio()).isEqualTo(1L);
    }

    @Test
    void findAllByRestaurantIdOrderByFolioDesc_ReturnsMostRecentFirst() {
        // Arrange
        saveInvoice("invoice-1", createOrder("order-1"), 1L, false);
        saveInvoice("invoice-2", createOrder("order-2"), 3L, false);
        saveInvoice("invoice-3", createOrder("order-3"), 2L, false);

        // Act
        List<Invoice> result =
                invoiceRepository.findAllByRestaurantIdOrderByFolioDesc(restaurantId);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getFolio()).isEqualTo(3L);
    }

    @Test
    void findByRestaurantIdAndIsPaid_FiltersByPaymentStatus() {
        // Arrange
        saveInvoice("invoice-1", createOrder("order-1"), 1L, true);
        saveInvoice("invoice-2", createOrder("order-2"), 2L, false);

        // Act
        List<Invoice> result = invoiceRepository.findByRestaurantIdAndIsPaid(restaurantId, false);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFolio()).isEqualTo(2L);
    }

    @Test
    void countUnpaidByOrderIdAndRestaurantId_ReturnsCorrectCount() {
        // Arrange
        String orderId = createOrder("order-1");
        saveInvoice("invoice-1", orderId, 1L, false);
        saveInvoice("invoice-2", createOrder("order-2"), 2L, true);
        saveInvoice("invoice-3", orderId, 3L, false);

        // Act
        long count = invoiceRepository.countUnpaidByOrderIdAndRestaurantId(orderId, restaurantId);

        // Assert
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByRestaurantId_ReturnsTotalInvoices() {
        // Arrange
        saveInvoice("invoice-1", createOrder("order-1"), 1L, false);
        saveInvoice("invoice-2", createOrder("order-2"), 2L, false);

        // Act
        long count = invoiceRepository.countByRestaurantId(restaurantId);

        // Assert
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findByRestaurantIdAndFolio_ReturnsMatchingInvoice() {
        // Arrange
        saveInvoice("invoice-1", createOrder("order-1"), 42L, false);

        // Act
        Optional<Invoice> result = invoiceRepository.findByRestaurantIdAndFolio(restaurantId, 42L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("invoice-1");
    }

    @Test
    void lockByRestaurantId_WithExistingSequence_ReturnsSequence() {
        // Arrange
        FolioSequence seq =
                FolioSequence.builder().restaurantId(restaurantId).nextFolio(10L).build();
        folioSequenceRepository.save(seq);

        // Act
        Optional<FolioSequence> result = folioSequenceRepository.lockByRestaurantId(restaurantId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getNextFolio()).isEqualTo(10L);
    }

    private String createOrder(String id) {
        orderSequence++;
        Order order =
                Order.builder()
                        .id(id)
                        .restaurantId(restaurantId)
                        .num(orderSequence)
                        .type("IN_PLACE")
                        .status("PENDING")
                        .people(1)
                        .total(BigDecimal.valueOf(100))
                        .userId(userId)
                        .build();
        return orderRepository.save(order).getId();
    }

    private void saveInvoice(String id, String orderId, Long folio, boolean paid) {
        Invoice invoice =
                Invoice.builder()
                        .id(id)
                        .restaurantId(restaurantId)
                        .orderId(orderId)
                        .folio(folio)
                        .subtotal(BigDecimal.valueOf(86.21))
                        .tax(BigDecimal.valueOf(13.79))
                        .total(BigDecimal.valueOf(100.00))
                        .isPaid(paid)
                        .build();
        invoiceRepository.save(invoice);
    }
}
