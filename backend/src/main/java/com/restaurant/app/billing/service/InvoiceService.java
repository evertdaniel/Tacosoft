package com.restaurant.app.billing.service;

import com.restaurant.app.billing.dto.CreateInvoiceRequest;
import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.dto.PaymentRequest;
import com.restaurant.app.billing.model.FolioSequence;
import com.restaurant.app.billing.model.Invoice;
import com.restaurant.app.billing.repository.FolioSequenceRepository;
import com.restaurant.app.billing.repository.InvoiceRepository;
import com.restaurant.app.cash.model.Transaction;
import com.restaurant.app.cash.repository.CashRegisterRepository;
import com.restaurant.app.cash.repository.TransactionRepository;
import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Invoice service - billing operations with financial invariants. Implements INV-02 (contiguous
 * folios) and INV-03 (idempotent payments).
 */
@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final FolioSequenceRepository folioSequenceRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final CashRegisterRepository cashRegisterRepository;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            FolioSequenceRepository folioSequenceRepository,
            OrderRepository orderRepository,
            TransactionRepository transactionRepository,
            CashRegisterRepository cashRegisterRepository) {
        this.invoiceRepository = invoiceRepository;
        this.folioSequenceRepository = folioSequenceRepository;
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.cashRegisterRepository = cashRegisterRepository;
    }

    /**
     * Create invoice with contiguous folio assignment. Implements INV-02 with pessimistic locking.
     *
     * <p>Transaction isolation: REPEATABLE_READ prevents phantom reads. Lock mode:
     * PESSIMISTIC_WRITE ensures exclusive folio sequence access.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public InvoiceDto createInvoice(CreateInvoiceRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        // 1. Fetch and validate order
        Order order =
                orderRepository
                        .findByIdAndRestaurantId(request.getOrderId(), restaurantId)
                        .orElseThrow(() -> new NotFoundException("Order", request.getOrderId()));

        // 2. Lock folio sequence for this restaurant (INV-02)
        FolioSequence folioSeq =
                folioSequenceRepository
                        .lockByRestaurantId(restaurantId)
                        .orElseGet(
                                () -> {
                                    // First invoice for this restaurant - create sequence
                                    FolioSequence newSeq =
                                            FolioSequence.builder()
                                                    .restaurantId(restaurantId)
                                                    .nextFolio(1L)
                                                    .build();
                                    return folioSequenceRepository.save(newSeq);
                                });

        // 3. Assign folio atomically
        Long folio = folioSeq.getAndIncrement();
        folioSequenceRepository.save(folioSeq);

        // 4. Calculate invoice totals from order
        BigDecimal total = order.getTotal();
        BigDecimal subtotal =
                total.divide(
                        BigDecimal.valueOf(1.16), 2, BigDecimal.ROUND_HALF_UP); // Assuming 16% tax
        BigDecimal tax = total.subtract(subtotal);

        // 5. Create invoice
        Invoice invoice =
                Invoice.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .orderId(order.getId())
                        .folio(folio)
                        .subtotal(subtotal)
                        .tax(tax)
                        .total(total)
                        .isPaid(false)
                        .build();

        invoice = invoiceRepository.save(invoice);

        return toDto(invoice);
    }

    /**
     * Record payment with idempotency. Implements INV-03 via UNIQUE (reference_id) constraint.
     *
     * <p>Duplicate payment calls return existing transaction (idempotent).
     */
    @Transactional
    public InvoiceDto payInvoice(String invoiceId, PaymentRequest request, String userId) {
        String restaurantId = TenantContext.getRestaurantId();

        // 1. Fetch invoice
        Invoice invoice =
                invoiceRepository
                        .findByIdAndRestaurantId(invoiceId, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Invoice", invoiceId));

        if (invoice.getIsPaid()) {
            // Already paid - check if this is a duplicate call
            if (transactionRepository.existsByReferenceId(invoiceId)) {
                // Idempotent - return existing state
                return toDto(invoice);
            }
        }

        // 2. Validate cash register is open (INV-05)
        var cashRegister =
                cashRegisterRepository
                        .findOpenByUserIdAndRestaurantId(userId, restaurantId)
                        .orElseThrow(
                                () ->
                                        new ConflictException(
                                                "No open cash register found for user"));

        // 3. Check for duplicate payment (INV-03)
        if (transactionRepository.existsByReferenceId(invoiceId)) {
            // Idempotent - return existing invoice
            invoice = invoiceRepository.findById(invoiceId).get();
            return toDto(invoice);
        }

        // 4. Create transaction (unique constraint prevents duplicates)
        Transaction transaction =
                Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .cashRegisterId(cashRegister.getId())
                        .type("INCOME")
                        .amount(request.getAmount())
                        .paymentMethod(request.getPaymentMethod())
                        .referenceId(invoiceId) // INV-03: Unique constraint prevents double payment
                        .description("Payment for invoice " + invoice.getFolio())
                        .build();

        transactionRepository.save(transaction);

        // 5. Update invoice status
        invoice.setIsPaid(true);
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice = invoiceRepository.save(invoice);

        // 6. Update order.isPaid if all invoices for the order are paid (BILL-008)
        updateOrderPaidStatus(invoice.getOrderId(), restaurantId);

        return toDto(invoice);
    }

    private void updateOrderPaidStatus(String orderId, String restaurantId) {
        long unpaidInvoices =
                invoiceRepository.countUnpaidByOrderIdAndRestaurantId(orderId, restaurantId);
        if (unpaidInvoices == 0) {
            Order order =
                    orderRepository
                            .findByIdAndRestaurantId(orderId, restaurantId)
                            .orElseThrow(() -> new NotFoundException("Order", orderId));
            order.setIsPaid(true);
            orderRepository.save(order);
        }
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoice(String invoiceId) {
        String restaurantId = TenantContext.getRestaurantId();

        Invoice invoice =
                invoiceRepository
                        .findByIdAndRestaurantId(invoiceId, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Invoice", invoiceId));

        return toDto(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> listInvoices() {
        String restaurantId = TenantContext.getRestaurantId();

        return invoiceRepository.findAllByRestaurantId(restaurantId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> listUnpaidInvoices() {
        String restaurantId = TenantContext.getRestaurantId();

        return invoiceRepository.findByRestaurantIdAndIsPaid(restaurantId, false).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private InvoiceDto toDto(Invoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .restaurantId(invoice.getRestaurantId())
                .orderId(invoice.getOrderId())
                .folio(invoice.getFolio())
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .total(invoice.getTotal())
                .isPaid(invoice.getIsPaid())
                .paymentMethod(invoice.getPaymentMethod())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
