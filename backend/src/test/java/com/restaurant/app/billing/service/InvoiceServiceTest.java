package com.restaurant.app.billing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.billing.dto.CreateInvoiceRequest;
import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.dto.PaymentRequest;
import com.restaurant.app.billing.model.FolioSequence;
import com.restaurant.app.billing.model.Invoice;
import com.restaurant.app.billing.repository.FolioSequenceRepository;
import com.restaurant.app.billing.repository.InvoiceRepository;
import com.restaurant.app.cash.model.CashRegister;
import com.restaurant.app.cash.repository.CashRegisterRepository;
import com.restaurant.app.cash.repository.TransactionRepository;
import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.util.List;
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

    @Test
    void createInvoice_FirstInvoice_AssignsFolioOneAndCalculatesTax() {
        // Arrange
        Order order = createUnpaidOrder();
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setOrderId(orderId);

        when(orderRepository.findByIdAndRestaurantId(orderId, restaurantId))
                .thenReturn(Optional.of(order));
        when(folioSequenceRepository.lockByRestaurantId(restaurantId)).thenReturn(Optional.empty());
        when(folioSequenceRepository.save(any(FolioSequence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        InvoiceDto result = invoiceService.createInvoice(request);

        // Assert
        assertThat(result.getFolio()).isEqualTo(1L);
        assertThat(result.getTotal()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(result.getSubtotal()).isEqualTo(BigDecimal.valueOf(86.21));
        assertThat(result.getTax()).isEqualTo(BigDecimal.valueOf(13.79));
    }

    @Test
    void createInvoice_ExistingSequence_AssignsNextFolio() {
        // Arrange
        Order order = createUnpaidOrder();
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setOrderId(orderId);

        FolioSequence seq =
                FolioSequence.builder().restaurantId(restaurantId).nextFolio(42L).build();

        when(orderRepository.findByIdAndRestaurantId(orderId, restaurantId))
                .thenReturn(Optional.of(order));
        when(folioSequenceRepository.lockByRestaurantId(restaurantId)).thenReturn(Optional.of(seq));
        when(folioSequenceRepository.save(any(FolioSequence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        InvoiceDto result = invoiceService.createInvoice(request);

        // Assert
        assertThat(result.getFolio()).isEqualTo(42L);
        assertThat(seq.getNextFolio()).isEqualTo(43L);
    }

    @Test
    void createInvoice_OrderNotFound_ThrowsNotFoundException() {
        // Arrange
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setOrderId(orderId);

        when(orderRepository.findByIdAndRestaurantId(orderId, restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> invoiceService.createInvoice(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order");
    }

    @Test
    void getInvoice_ExistingInvoice_ReturnsInvoice() {
        // Arrange
        Invoice invoice = createUnpaidInvoice();
        when(invoiceRepository.findByIdAndRestaurantId(invoiceId, restaurantId))
                .thenReturn(Optional.of(invoice));

        // Act
        InvoiceDto result = invoiceService.getInvoice(invoiceId);

        // Assert
        assertThat(result.getId()).isEqualTo(invoiceId);
        assertThat(result.getFolio()).isEqualTo(1L);
    }

    @Test
    void getInvoice_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(invoiceRepository.findByIdAndRestaurantId(invoiceId, restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> invoiceService.getInvoice(invoiceId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listInvoices_ReturnsAllRestaurantInvoices() {
        // Arrange
        Invoice invoice = createUnpaidInvoice();
        when(invoiceRepository.findAllByRestaurantId(restaurantId)).thenReturn(List.of(invoice));

        // Act
        List<InvoiceDto> result = invoiceService.listInvoices();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(invoiceId);
    }

    @Test
    void listUnpaidInvoices_ReturnsOnlyUnpaidInvoices() {
        // Arrange
        Invoice unpaid = createUnpaidInvoice();
        when(invoiceRepository.findByRestaurantIdAndIsPaid(restaurantId, false))
                .thenReturn(List.of(unpaid));

        // Act
        List<InvoiceDto> result = invoiceService.listUnpaidInvoices();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsPaid()).isFalse();
    }

    @Test
    void payInvoice_AlreadyPaidWithTransaction_ReturnsInvoiceIdempotently() {
        // Arrange
        Invoice paidInvoice = createUnpaidInvoice();
        paidInvoice.setIsPaid(true);
        when(invoiceRepository.findByIdAndRestaurantId(invoiceId, restaurantId))
                .thenReturn(Optional.of(paidInvoice));
        when(transactionRepository.existsByReferenceId(invoiceId)).thenReturn(true);

        // Act
        InvoiceDto result = invoiceService.payInvoice(invoiceId, createPaymentRequest(), userId);

        // Assert
        assertThat(result.getIsPaid()).isTrue();
        verify(cashRegisterRepository, never())
                .findOpenByUserIdAndRestaurantId(anyString(), anyString());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void payInvoice_NoOpenCashRegister_ThrowsConflictException() {
        // Arrange
        Invoice invoice = createUnpaidInvoice();
        when(invoiceRepository.findByIdAndRestaurantId(invoiceId, restaurantId))
                .thenReturn(Optional.of(invoice));
        when(cashRegisterRepository.findOpenByUserIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(
                        () -> invoiceService.payInvoice(invoiceId, createPaymentRequest(), userId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("No open cash register");
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
