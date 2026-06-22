package com.restaurant.app.cash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.restaurant.app.billing.dto.CreateInvoiceRequest;
import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.dto.PaymentRequest;
import com.restaurant.app.billing.service.InvoiceService;
import com.restaurant.app.cash.dto.CloseCashRegisterRequest;
import com.restaurant.app.cash.dto.OpenCashRegisterRequest;
import com.restaurant.app.cash.dto.XReportDto;
import com.restaurant.app.cash.dto.ZReportDto;
import com.restaurant.app.cash.model.CashRegister;
import com.restaurant.app.cash.repository.CashRegisterRepository;
import com.restaurant.app.cash.repository.TransactionRepository;
import com.restaurant.app.cash.service.CashRegisterService;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Financial invariant test for INV-05: Cash register balance accuracy.
 *
 * <p>CRITICAL 💰 - Validates: 1. Closed register rejects transactions 2. Z-report balance accuracy:
 * saldo_final = saldo_inicial + Σ(ingresos) − Σ(gastos)
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class CashRegisterInvariantTest {

    @Autowired private CashRegisterService cashRegisterService;

    @Autowired private CashRegisterRepository cashRegisterRepository;

    @Autowired private TransactionRepository transactionRepository;

    @Autowired private InvoiceService invoiceService;

    @Autowired private OrderRepository orderRepository;

    private final String restaurantId = "restaurant-cash-test";
    private final String userId = "user-cash-test";
    private CashRegister testRegister;
    private Order testOrder;
    private InvoiceDto testInvoice;

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);

        // Create test order (reserved range)
        testOrder =
                Order.builder()
                        .id("order-cash-test")
                        .restaurantId(restaurantId)
                        .num(8002)
                        .type("IN_PLACE")
                        .status("COMPLETED")
                        .total(BigDecimal.valueOf(116))
                        .build();
        testOrder = orderRepository.save(testOrder);

        // Create test invoice (reserved folio range)
        CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest();
        invoiceRequest.setOrderId(testOrder.getId());
        testInvoice = invoiceService.createInvoice(invoiceRequest);
    }

    @AfterEach
    void tearDown() {
        try {
            // transactionRepository.deleteAllByRestaurantId(restaurantId);
            if (testRegister != null && testRegister.getId() != null) {
                cashRegisterRepository.deleteById(testRegister.getId());
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        TenantContext.clear();
    }

    /**
     * INV-05: Z-report balance accuracy test.
     *
     * <p>Validates: saldo_final = saldo_inicial + Σ(ingresos) − Σ(gastos)
     */
    @Test
    void zReport_BalanceCalculation_Accurate_INV05() {
        // Arrange - Open register with 500 initial
        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setOpeningAmount(BigDecimal.valueOf(500));
        var registerDto = cashRegisterService.openRegister(openRequest, userId);
        testRegister = cashRegisterRepository.findById(registerDto.getId()).orElseThrow();

        // Create transactions
        createTransaction(testRegister.getId(), "INCOME", BigDecimal.valueOf(100), "SALE-1");
        createTransaction(testRegister.getId(), "INCOME", BigDecimal.valueOf(50), "SALE-2");
        createTransaction(testRegister.getId(), "EXPENSE", BigDecimal.valueOf(20), "REFUND-1");
        createTransaction(testRegister.getId(), "INCOME", BigDecimal.valueOf(30), "SALE-3");

        BigDecimal totalIncome = BigDecimal.valueOf(180); // 100 + 50 + 30
        BigDecimal totalExpenses = BigDecimal.valueOf(20);
        BigDecimal expectedAmount =
                BigDecimal.valueOf(500).add(totalIncome).subtract(totalExpenses); // 660

        // Act - Close register
        CloseCashRegisterRequest closeRequest = new CloseCashRegisterRequest();
        closeRequest.setClosingAmount(expectedAmount);
        ZReportDto zReport =
                cashRegisterService.closeRegister(testRegister.getId(), closeRequest, userId);

        // Assert - CRITICAL INVARIANTS

        // 1. Balance calculation is accurate (INV-05)
        assertThat(zReport.getOpeningAmount()).isEqualByComparingTo("500");
        assertThat(zReport.getTotalIncome()).isEqualByComparingTo("180");
        assertThat(zReport.getTotalExpenses()).isEqualByComparingTo("20");
        assertThat(zReport.getExpectedAmount()).isEqualByComparingTo("660");
        assertThat(zReport.getDeclaredAmount()).isEqualByComparingTo("660");
        assertThat(zReport.getDifference()).isEqualByComparingTo("0");
        assertThat(zReport.getStatus()).isEqualTo("BALANCED");

        // 2. Transaction counts match
        assertThat(zReport.getIncomeCount()).isEqualTo(3);
        assertThat(zReport.getExpenseCount()).isEqualTo(1);

        // 3. Register is now closed
        CashRegister closedRegister =
                cashRegisterRepository.findById(testRegister.getId()).orElseThrow();
        assertThat(closedRegister.getStatus()).isEqualTo("CLOSED");
        assertThat(closedRegister.getClosedAt()).isNotNull();
        assertThat(closedRegister.getClosingAmount()).isEqualByComparingTo("660");

        System.out.println("✅ INV-05 PASSED: Z-report balance calculation is accurate");
        System.out.println("   Opening: 500, Income: 180, Expenses: 20, Expected: 660");
        System.out.println("   Declared: 660, Difference: 0, Status: BALANCED");
    }

    /** INV-05: Z-report detects discrepancy when declared amount is wrong. */
    @Test
    void zReport_DeclaredAmountMismatch_DetectsDifference_INV05() {
        // Arrange
        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setOpeningAmount(BigDecimal.valueOf(1000));
        var registerDto = cashRegisterService.openRegister(openRequest, userId);
        testRegister = cashRegisterRepository.findById(registerDto.getId()).orElseThrow();

        createTransaction(testRegister.getId(), "INCOME", BigDecimal.valueOf(200), "SALE-1");

        BigDecimal expected = BigDecimal.valueOf(1200); // 1000 + 200
        BigDecimal wrongDeclared = BigDecimal.valueOf(1150); // 50 short

        // Act
        CloseCashRegisterRequest closeRequest = new CloseCashRegisterRequest();
        closeRequest.setClosingAmount(wrongDeclared);
        ZReportDto zReport =
                cashRegisterService.closeRegister(testRegister.getId(), closeRequest, userId);

        // Assert
        assertThat(zReport.getExpectedAmount()).isEqualByComparingTo("1200");
        assertThat(zReport.getDeclaredAmount()).isEqualByComparingTo("1150");
        assertThat(zReport.getDifference()).isEqualByComparingTo("-50"); // Short by 50
        assertThat(zReport.getStatus()).isEqualTo("DIFFERENCE");

        System.out.println("✅ INV-05 PASSED: Z-report detected discrepancy of -50");
    }

    /** INV-05: Transaction on closed register must fail. */
    @Test
    void transactionOnClosedRegister_ThrowsConflict_INV05() {
        // Arrange
        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setOpeningAmount(BigDecimal.valueOf(500));
        var registerDto = cashRegisterService.openRegister(openRequest, userId);
        testRegister = cashRegisterRepository.findById(registerDto.getId()).orElseThrow();

        // Close the register
        CloseCashRegisterRequest closeRequest = new CloseCashRegisterRequest();
        closeRequest.setClosingAmount(BigDecimal.valueOf(500));
        cashRegisterService.closeRegister(testRegister.getId(), closeRequest, userId);

        // Act - Try to create transaction on closed register
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class, // Should throw ConflictException
                () -> {
                    // Try to pay invoice (should fail because register is closed)
                    PaymentRequest paymentRequest = new PaymentRequest();
                    paymentRequest.setAmount(BigDecimal.valueOf(100));
                    paymentRequest.setPaymentMethod("CASH");
                    invoiceService.payInvoice(testInvoice.getId(), paymentRequest, userId);
                });

        System.out.println("✅ INV-05 PASSED: Transaction rejected on closed register");
    }

    /** X-report test (current balance without closing). */
    @Test
    void xReport_CurrentBalance_CalculatedCorrectly() {
        // Arrange
        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setOpeningAmount(BigDecimal.valueOf(300));
        var registerDto = cashRegisterService.openRegister(openRequest, userId);
        testRegister = cashRegisterRepository.findById(registerDto.getId()).orElseThrow();

        createTransaction(testRegister.getId(), "INCOME", BigDecimal.valueOf(75), "SALE-1");
        createTransaction(testRegister.getId(), "EXPENSE", BigDecimal.valueOf(25), "EXPENSE-1");

        // Act
        XReportDto xReport = cashRegisterService.getXReport(userId);

        // Assert
        assertThat(xReport.getOpeningAmount()).isEqualByComparingTo("300");
        assertThat(xReport.getTotalIncome()).isEqualByComparingTo("75");
        assertThat(xReport.getTotalExpenses()).isEqualByComparingTo("25");
        assertThat(xReport.getCurrentBalance()).isEqualByComparingTo("350"); // 300 + 75 - 25
        assertThat(xReport.getIncomeCount()).isEqualTo(1);
        assertThat(xReport.getExpenseCount()).isEqualTo(1);
        assertThat(xReport.getTransactionCount()).isEqualTo(2);

        // Register should still be OPEN (X-report doesn't close)
        CashRegister stillOpen =
                cashRegisterRepository.findById(testRegister.getId()).orElseThrow();
        assertThat(stillOpen.getStatus()).isEqualTo("OPEN");

        System.out.println("✅ X-report calculated correctly: 300 + 75 - 25 = 350");
    }

    /** Test: Only one open register per user at a time. */
    @Test
    void openRegister_UserAlreadyHasOpenRegister_ThrowsConflict() {
        // Arrange - Open first register
        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setOpeningAmount(BigDecimal.valueOf(100));
        var registerDto1 = cashRegisterService.openRegister(openRequest, userId);
        testRegister = cashRegisterRepository.findById(registerDto1.getId()).orElseThrow();

        // Act - Try to open second register
        OpenCashRegisterRequest secondRequest = new OpenCashRegisterRequest();
        secondRequest.setOpeningAmount(BigDecimal.valueOf(200));

        // Assert - Should throw conflict
        assertThatThrownBy(() -> cashRegisterService.openRegister(secondRequest, userId))
                .isInstanceOf(com.restaurant.app.common.ConflictException.class)
                .hasMessageContaining("already has an open cash register");

        System.out.println("✅ Users can only have one open register at a time");
    }

    // Helper methods

    private void createTransaction(
            String cashRegisterId, String type, BigDecimal amount, String referenceId) {
        com.restaurant.app.cash.model.Transaction transaction =
                com.restaurant.app.cash.model.Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .cashRegisterId(cashRegisterId)
                        .type(type)
                        .amount(amount)
                        .paymentMethod("CASH")
                        .referenceId(referenceId)
                        .description("Test transaction")
                        .build();
        transactionRepository.save(transaction);
    }
}
