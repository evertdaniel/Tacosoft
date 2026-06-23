package com.restaurant.app.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.restaurant.app.MySQLTestcontainersConfig;
import com.restaurant.app.billing.dto.CreateInvoiceRequest;
import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.model.FolioSequence;
import com.restaurant.app.billing.repository.FolioSequenceRepository;
import com.restaurant.app.billing.repository.InvoiceRepository;
import com.restaurant.app.billing.service.InvoiceService;
import com.restaurant.app.common.EnabledIfDockerAvailable;
import com.restaurant.app.common.IntegrationTestFixtures;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Financial invariant test for INV-02: Contiguous folios without gaps/duplicates.
 *
 * <p>CRITICAL 💰 - Validates core financial invariant with 1000 concurrent threads. This test MUST
 * pass before production deployment.
 */
@Tag("integration")
@EnabledIfDockerAvailable
@SpringBootTest
@ActiveProfiles("test")
@Import(MySQLTestcontainersConfig.class)
class InvoiceFinancialInvariantTest {

    @Autowired private InvoiceService invoiceService;

    @Autowired private InvoiceRepository invoiceRepository;

    @Autowired private FolioSequenceRepository folioSequenceRepository;

    @Autowired private OrderRepository orderRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    private String restaurantId;
    private String userId;
    private String personId;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        personId = UUID.randomUUID().toString();

        // Set tenant context
        TenantContext.setRestaurantId(restaurantId);

        // Create the minimum tenant/user rows required by FK/NOT NULL constraints
        IntegrationTestFixtures.createRestaurant(jdbcTemplate, restaurantId, "Folio Test");
        IntegrationTestFixtures.createPerson(jdbcTemplate, personId, "Folio", "Test");
        IntegrationTestFixtures.createAppUser(
                jdbcTemplate,
                userId,
                "folio-user-" + UUID.randomUUID(),
                "password",
                personId,
                true);

        // Create test order in reserved ID block (8000-8999 for testing)
        testOrder =
                Order.builder()
                        .id("order-test-" + System.currentTimeMillis())
                        .restaurantId(restaurantId)
                        .num(8000 + (int) (System.currentTimeMillis() % 999)) // Reserved test range
                        .type("IN_PLACE")
                        .status("CLOSED")
                        .people(1)
                        .total(BigDecimal.valueOf(116)) // 100 subtotal + 16 tax
                        .userId(userId)
                        .build();

        testOrder = orderRepository.save(testOrder);

        // Initialize folio sequence to start at 90000 (reserved test block)
        FolioSequence sequence =
                folioSequenceRepository
                        .findById(restaurantId)
                        .orElseGet(
                                () -> {
                                    FolioSequence newSeq = new FolioSequence();
                                    newSeq.setRestaurantId(restaurantId);
                                    return newSeq;
                                });
        sequence.setNextFolio(90000L); // Reserved test block: 90000-90999
        folioSequenceRepository.save(sequence);
    }

    @AfterEach
    void tearDown() {
        // Cleanup test data
        try {
            invoiceRepository
                    .findAllByRestaurantId(restaurantId)
                    .forEach(invoice -> invoiceRepository.deleteById(invoice.getId()));
            folioSequenceRepository.deleteById(restaurantId);
            if (testOrder != null && testOrder.getId() != null) {
                orderRepository.deleteById(testOrder.getId());
            }
            IntegrationTestFixtures.cleanupUserAndRestaurant(
                    jdbcTemplate, userId, personId, restaurantId);
        } catch (Exception e) {
            // Ignore cleanup errors in tests
        }
        TenantContext.clear();
    }

    /**
     * INV-02: Contiguous folios test.
     *
     * <p>Creates 1000 invoices concurrently using ExecutorService. Verifies: 1. Exactly 1000
     * invoices created 2. Folios are contiguous (no gaps) 3. No duplicate folios 4. Folios range
     * from 90000 to 90999
     *
     * <p>This is a CRITICAL financial invariant test.
     */
    @Test
    void invoiceFolioConcurrency_1000Threads_ContiguousFoliosNoGapsNoDuplicates() throws Exception {
        // Arrange
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Future<InvoiceDto>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Act - Launch 1000 concurrent invoice creation threads
        for (int i = 0; i < threadCount; i++) {
            Future<InvoiceDto> future =
                    executorService.submit(
                            () -> {
                                try {
                                    // Wait for all threads to be ready (maximize concurrency)
                                    startLatch.await();

                                    // Create invoice
                                    CreateInvoiceRequest request = new CreateInvoiceRequest();
                                    request.setOrderId(testOrder.getId());
                                    InvoiceDto invoice = invoiceService.createInvoice(request);

                                    successCount.incrementAndGet();
                                    return invoice;
                                } catch (Exception e) {
                                    errorCount.incrementAndGet();
                                    throw new RuntimeException(e);
                                } finally {
                                    endLatch.countDown();
                                }
                            });
            futures.add(future);
        }

        // Signal all threads to start simultaneously
        startLatch.countDown();

        // Wait for all threads to complete (10 second timeout)
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "All threads should complete within timeout");

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Collect results
        List<Long> folios = new ArrayList<>();
        for (Future<InvoiceDto> future : futures) {
            try {
                InvoiceDto invoice = future.get();
                folios.add(invoice.getFolio());
            } catch (ExecutionException e) {
                // Record error but continue checking other results
                System.err.println("Thread failed: " + e.getCause().getMessage());
            }
        }

        // Assert - CRITICAL INVARIANTS

        // 1. Exactly 1000 invoices created successfully
        assertThat(successCount.get()).isEqualTo(1000);
        assertThat(errorCount.get()).isEqualTo(0);
        assertThat(folios).hasSize(1000);

        // 2. No duplicate folios (INV-02)
        List<Long> uniqueFolios = folios.stream().distinct().toList();
        assertThat(uniqueFolios).hasSize(1000); // All folios are unique

        // 3. Folios are contiguous - no gaps (INV-02)
        long minFolio = folios.stream().min(Long::compareTo).orElse(0L);
        long maxFolio = folios.stream().max(Long::compareTo).orElse(0L);
        assertThat(maxFolio - minFolio + 1).isEqualTo(1000); // Contiguous range

        // 4. Folios are in expected test range (90000-90999)
        assertThat(minFolio).isGreaterThanOrEqualTo(90000L);
        assertThat(maxFolio).isLessThanOrEqualTo(90999L);

        // 5. Verify database state matches
        List<com.restaurant.app.billing.model.Invoice> allInvoices =
                invoiceRepository.findAllByRestaurantId(restaurantId);
        assertThat(allInvoices).hasSize(1000);

        // 6. Verify folio sequence was incremented correctly
        FolioSequence sequence = folioSequenceRepository.findById(restaurantId).orElseThrow();
        assertThat(sequence.getNextFolio()).isEqualTo(91000L); // 90000 + 1000

        System.out.println(
                "✅ INV-02 PASSED: 1000 concurrent invoices created with contiguous folios");
        System.out.println("   Folio range: " + minFolio + " - " + maxFolio);
        System.out.println("   Success: " + successCount.get() + ", Errors: " + errorCount.get());
    }

    /** Test that folio sequence is properly locked and prevents gaps. */
    @Test
    void folioSequenceLock_PessimisticLock_PreventsGaps() {
        // Arrange - Create sequence
        FolioSequence sequence = new FolioSequence();
        sequence.setRestaurantId(restaurantId);
        sequence.setNextFolio(90000L);
        folioSequenceRepository.save(sequence);

        // Act - Lock and increment multiple times
        Long folio1 =
                folioSequenceRepository
                        .lockByRestaurantId(restaurantId)
                        .orElseThrow()
                        .getAndIncrement();
        folioSequenceRepository.save(sequence);

        Long folio2 =
                folioSequenceRepository
                        .lockByRestaurantId(restaurantId)
                        .orElseThrow()
                        .getAndIncrement();
        folioSequenceRepository.save(sequence);

        Long folio3 =
                folioSequenceRepository
                        .lockByRestaurantId(restaurantId)
                        .orElseThrow()
                        .getAndIncrement();
        folioSequenceRepository.save(sequence);

        // Assert - Contiguous folios
        assertThat(folio1).isEqualTo(90000L);
        assertThat(folio2).isEqualTo(90001L);
        assertThat(folio3).isEqualTo(90002L);

        // Verify sequence state
        FolioSequence updated = folioSequenceRepository.findById(restaurantId).orElseThrow();
        assertThat(updated.getNextFolio()).isEqualTo(90003L);
    }

    /** Test UNIQUE constraint on (restaurant_id, folio) prevents duplicates. */
    @Test
    void uniqueFolioConstraint_PreventsDuplicateFolios() {
        // Arrange - Create invoice with folio 90000
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setOrderId(testOrder.getId());
        InvoiceDto invoice1 = invoiceService.createInvoice(request);

        // Manually try to create another invoice with same folio (should fail)
        // This simulates what would happen if the pessimistic lock was bypassed
        com.restaurant.app.billing.model.Invoice duplicateInvoice =
                com.restaurant.app.billing.model.Invoice.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .orderId(testOrder.getId())
                        .folio(90000L) // Duplicate folio
                        .subtotal(BigDecimal.valueOf(100))
                        .tax(BigDecimal.valueOf(16))
                        .total(BigDecimal.valueOf(116))
                        .isPaid(false)
                        .build();

        // Act & Assert - Should throw constraint violation
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class, () -> invoiceRepository.save(duplicateInvoice));
    }
}
