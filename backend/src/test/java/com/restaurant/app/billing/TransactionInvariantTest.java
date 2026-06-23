package com.restaurant.app.billing;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.billing.dto.CreateInvoiceRequest;
import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.dto.PaymentRequest;
import com.restaurant.app.billing.model.Invoice;
import com.restaurant.app.billing.repository.InvoiceRepository;
import com.restaurant.app.billing.service.InvoiceService;
import com.restaurant.app.cash.model.CashRegister;
import com.restaurant.app.cash.repository.CashRegisterRepository;
import com.restaurant.app.cash.repository.TransactionRepository;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Financial invariant test for INV-03: Idempotent payments.
 *
 * <p>CRITICAL 💰 - Validates that duplicate payment calls create only one transaction. UNIQUE
 * (reference_id) constraint prevents double payment.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class TransactionInvariantTest {

    @Autowired private InvoiceService invoiceService;

    @Autowired private InvoiceRepository invoiceRepository;

    @Autowired private TransactionRepository transactionRepository;

    @Autowired private CashRegisterRepository cashRegisterRepository;

    @Autowired private OrderRepository orderRepository;

    private final String restaurantId = "restaurant-payment-test";
    private final String userId = "user-payment-test";
    private Order testOrder;
    private InvoiceDto testInvoice;
    private CashRegister testRegister;

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);

        // Create test order (reserved range 8000-8999)
        testOrder =
                Order.builder()
                        .id("order-payment-test")
                        .restaurantId(restaurantId)
                        .num(8001)
                        .type("IN_PLACE")
                        .status("COMPLETED")
                        .people(1)
                        .total(BigDecimal.valueOf(116))
                        .build();
        testOrder = orderRepository.save(testOrder);

        // Create test invoice (reserved folio range 90000-90999)
        CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest();
        invoiceRequest.setOrderId(testOrder.getId());
        testInvoice = invoiceService.createInvoice(invoiceRequest);

        // Open cash register
        testRegister =
                CashRegister.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .userId(userId)
                        .openingAmount(BigDecimal.valueOf(500))
                        .status("OPEN")
                        .openedAt(LocalDateTime.now())
                        .build();
        testRegister = cashRegisterRepository.save(testRegister);
    }

    @AfterEach
    void tearDown() {
        try {
            if (testInvoice != null && testInvoice.getId() != null) {
                invoiceRepository.deleteById(testInvoice.getId());
            }
            if (testOrder != null && testOrder.getId() != null) {
                orderRepository.deleteById(testOrder.getId());
            }
            if (testRegister != null && testRegister.getId() != null) {
                cashRegisterRepository.deleteById(testRegister.getId());
            }
            // transactionRepository.deleteAllByCashRegisterId(testRegister.getId());
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        TenantContext.clear();
    }

    /**
     * INV-03: Idempotent payment test.
     *
     * <p>Calling payInvoice() multiple times with same invoice ID should: 1. Create only ONE
     * transaction record 2. Return same invoice state on subsequent calls 3. NOT throw errors
     * (idempotent)
     */
    @Test
    void payInvoice_DuplicateCalls_Idempotent_SingleTransactionCreated() {
        // Arrange
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(testInvoice.getTotal());
        paymentRequest.setPaymentMethod("CASH");

        // Act - Call payInvoice 5 times with same invoice
        InvoiceDto result1 = invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);
        InvoiceDto result2 = invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);
        InvoiceDto result3 = invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);
        InvoiceDto result4 = invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);
        InvoiceDto result5 = invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);

        // Assert - CRITICAL INVARIANTS

        // 1. All calls should succeed (no exceptions)
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();
        assertThat(result4).isNotNull();
        assertThat(result5).isNotNull();

        // 2. All returns should be identical (idempotent)
        assertThat(result1.getId()).isEqualTo(result2.getId());
        assertThat(result2.getId()).isEqualTo(result3.getId());
        assertThat(result3.getId()).isEqualTo(result4.getId());
        assertThat(result4.getId()).isEqualTo(result5.getId());

        assertThat(result1.getIsPaid()).isTrue();
        assertThat(result2.getIsPaid()).isTrue();
        assertThat(result3.getIsPaid()).isTrue();
        assertThat(result4.getIsPaid()).isTrue();
        assertThat(result5.getIsPaid()).isTrue();

        // 3. Only ONE transaction should exist (INV-03)
        List<com.restaurant.app.cash.model.Transaction> transactions =
                transactionRepository.findByCashRegisterId(testRegister.getId());
        assertThat(transactions).hasSize(1);

        com.restaurant.app.cash.model.Transaction transaction = transactions.get(0);
        assertThat(transaction.getReferenceId()).isEqualTo(testInvoice.getId());
        assertThat(transaction.getAmount()).isEqualTo(testInvoice.getTotal());
        assertThat(transaction.getType()).isEqualTo("INCOME");

        // 4. Invoice should be marked paid
        Invoice invoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(invoice.getIsPaid()).isTrue();
        assertThat(invoice.getPaymentMethod()).isEqualTo("CASH");

        System.out.println("✅ INV-03 PASSED: Duplicate payment calls are idempotent");
        System.out.println("   Invoice ID: " + testInvoice.getId());
        System.out.println("   Transactions created: " + transactions.size() + " (expected: 1)");
    }

    /** Test that UNIQUE (reference_id) constraint prevents duplicate transactions. */
    @Test
    void uniqueReferenceIdConstraint_PreventsDuplicateTransactions() {
        // Arrange - Create first payment
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(testInvoice.getTotal());
        paymentRequest.setPaymentMethod("CASH");
        invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);

        // Act - Try to manually create duplicate transaction with same reference_id
        com.restaurant.app.cash.model.Transaction duplicateTransaction =
                com.restaurant.app.cash.model.Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .cashRegisterId(testRegister.getId())
                        .type("INCOME")
                        .amount(testInvoice.getTotal())
                        .paymentMethod("CASH")
                        .referenceId(testInvoice.getId()) // Same reference_id
                        .description("Duplicate payment")
                        .build();

        // Assert - Should throw constraint violation
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class, () -> transactionRepository.save(duplicateTransaction));

        // Verify only one transaction exists
        List<com.restaurant.app.cash.model.Transaction> transactions =
                transactionRepository.findByCashRegisterId(testRegister.getId());
        assertThat(transactions).hasSize(1);
    }

    /** Test idempotency across different payment methods. */
    @Test
    void payInvoice_DifferentPaymentMethods_FirstCallWins() {
        // Arrange
        PaymentRequest cashRequest = new PaymentRequest();
        cashRequest.setAmount(testInvoice.getTotal());
        cashRequest.setPaymentMethod("CASH");

        PaymentRequest cardRequest = new PaymentRequest();
        cardRequest.setAmount(testInvoice.getTotal());
        cardRequest.setPaymentMethod("CARD");

        // Act - Pay with CASH first
        InvoiceDto result1 = invoiceService.payInvoice(testInvoice.getId(), cashRequest, userId);

        // Try to pay again with CARD (should be idempotent, keep CASH)
        InvoiceDto result2 = invoiceService.payInvoice(testInvoice.getId(), cardRequest, userId);

        // Assert - First payment method wins
        assertThat(result1.getPaymentMethod()).isEqualTo("CASH");
        assertThat(result2.getPaymentMethod()).isEqualTo("CASH"); // Not CARD

        // Only one transaction
        List<com.restaurant.app.cash.model.Transaction> transactions =
                transactionRepository.findByCashRegisterId(testRegister.getId());
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getPaymentMethod()).isEqualTo("CASH");
    }

    /** Test that idempotency works even after invoice is already paid. */
    @Test
    void payInvoice_AlreadyPaid_IdempotentReturnsCurrentState() {
        // Arrange
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(testInvoice.getTotal());
        paymentRequest.setPaymentMethod("CASH");

        // Act - Pay once
        InvoiceDto result1 = invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);

        // Pay again (already paid)
        InvoiceDto result2 = invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);

        // Assert - Should return same state without error
        assertThat(result1.getIsPaid()).isTrue();
        assertThat(result2.getIsPaid()).isTrue();

        // Only one transaction
        List<com.restaurant.app.cash.model.Transaction> transactions =
                transactionRepository.findByCashRegisterId(testRegister.getId());
        assertThat(transactions).hasSize(1);
    }
}
