package com.restaurant.app.billing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.dto.PaymentRequest;
import com.restaurant.app.billing.model.Invoice;
import com.restaurant.app.billing.repository.FolioSequenceRepository;
import com.restaurant.app.billing.repository.InvoiceRepository;
import com.restaurant.app.cash.model.CashRegister;
import com.restaurant.app.cash.repository.CashRegisterRepository;
import com.restaurant.app.cash.repository.TransactionRepository;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for InvoiceService. SPEC-BILL-001, INV-03, BILL-008. */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;

    @Mock private FolioSequenceRepository folioSequenceRepository;

    @Mock private OrderRepository orderRepository;

    @Mock private TransactionRepository transactionRepository;

    @Mock private CashRegisterRepository cashRegisterRepository;

    @InjectMocks private InvoiceService invoiceService;

    private final String restaurantId = "restaurant-1";
    private final String userId = "user-1";
    private final String orderId = "order-1";
    private final String invoiceId = "invoice-1";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void payInvoice_NoUnpaidInvoices_UpdatesOrderIsPaid() {
        // Arrange
        Invoice invoice = createUnpaidInvoice();
        Order order = createUnpaidOrder();
        CashRegister cashRegister = createOpenCashRegister();
        PaymentRequest request = createPaymentRequest();

        when(invoiceRepository.findByIdAndRestaurantId(invoiceId, restaurantId))
                .thenReturn(Optional.of(invoice));
        when(transactionRepository.existsByReferenceId(invoiceId)).thenReturn(false);
        when(cashRegisterRepository.findOpenByUserIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(cashRegister));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.countUnpaidByOrderIdAndRestaurantId(orderId, restaurantId))
                .thenReturn(0L);
        when(orderRepository.findByIdAndRestaurantId(orderId, restaurantId))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        InvoiceDto result = invoiceService.payInvoice(invoiceId, request, userId);

        // Assert
        assertThat(result.getIsPaid()).isTrue();
        assertThat(order.getIsPaid()).isTrue();
        verify(orderRepository).save(order);
    }

    @Test
    void payInvoice_OtherUnpaidInvoicesExist_DoesNotUpdateOrderIsPaid() {
        // Arrange
        Invoice invoice = createUnpaidInvoice();
        Order order = createUnpaidOrder();
        CashRegister cashRegister = createOpenCashRegister();
        PaymentRequest request = createPaymentRequest();

        when(invoiceRepository.findByIdAndRestaurantId(invoiceId, restaurantId))
                .thenReturn(Optional.of(invoice));
        when(transactionRepository.existsByReferenceId(invoiceId)).thenReturn(false);
        when(cashRegisterRepository.findOpenByUserIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(cashRegister));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.countUnpaidByOrderIdAndRestaurantId(orderId, restaurantId))
                .thenReturn(1L);

        // Act
        InvoiceDto result = invoiceService.payInvoice(invoiceId, request, userId);

        // Assert
        assertThat(result.getIsPaid()).isTrue();
        assertThat(order.getIsPaid()).isFalse();
        verify(orderRepository, never()).save(any(Order.class));
    }

    private Invoice createUnpaidInvoice() {
        return Invoice.builder()
                .id(invoiceId)
                .restaurantId(restaurantId)
                .orderId(orderId)
                .folio(1L)
                .subtotal(BigDecimal.valueOf(86.21))
                .tax(BigDecimal.valueOf(13.79))
                .total(BigDecimal.valueOf(100.00))
                .isPaid(false)
                .build();
    }

    private Order createUnpaidOrder() {
        return Order.builder()
                .id(orderId)
                .restaurantId(restaurantId)
                .num(1)
                .type("IN_PLACE")
                .status("PENDING")
                .total(BigDecimal.valueOf(100.00))
                .build();
    }

    private CashRegister createOpenCashRegister() {
        return CashRegister.builder()
                .id("register-1")
                .restaurantId(restaurantId)
                .userId(userId)
                .status("OPEN")
                .build();
    }

    private PaymentRequest createPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setPaymentMethod("CASH");
        return request;
    }
}
