package com.restaurant.app.cash.service;

import com.restaurant.app.cash.dto.CashRegisterDto;
import com.restaurant.app.cash.dto.CloseCashRegisterRequest;
import com.restaurant.app.cash.dto.OpenCashRegisterRequest;
import com.restaurant.app.cash.dto.XReportDto;
import com.restaurant.app.cash.dto.ZReportDto;
import com.restaurant.app.cash.model.CashRegister;
import com.restaurant.app.cash.repository.CashRegisterRepository;
import com.restaurant.app.cash.repository.TransactionRepository;
import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CashRegister service - cash drawer operations. Implements SPEC-CASH-001 and INV-05 (Z-report
 * balance validation).
 */
@Service
public class CashRegisterService {

    private final CashRegisterRepository cashRegisterRepository;
    private final TransactionRepository transactionRepository;

    public CashRegisterService(
            CashRegisterRepository cashRegisterRepository,
            TransactionRepository transactionRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.transactionRepository = transactionRepository;
    }

    /** Open a new cash register. Only one open register per restaurant at a time (INV-CASH-001). */
    @Transactional
    public CashRegisterDto openRegister(OpenCashRegisterRequest request, String userId) {
        String restaurantId = TenantContext.getRestaurantId();

        // Check if restaurant already has an open register
        List<CashRegister> openRegisters = cashRegisterRepository.findAllOpenByRestaurantId(restaurantId);
        if (!openRegisters.isEmpty()) {
            throw new ConflictException("Restaurant already has an open cash register");
        }

        CashRegister cashRegister =
                CashRegister.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .userId(userId)
                        .openingAmount(request.getOpeningAmount())
                        .status("OPEN")
                        .openedAt(LocalDateTime.now())
                        .build();

        cashRegister = cashRegisterRepository.save(cashRegister);

        return toDto(cashRegister);
    }

    /**
     * Close cash register with Z-report. Implements INV-05: validates balance accuracy.
     *
     * <p>Balance invariant: saldo_final = saldo_inicial + Σ(ingresos) − Σ(gastos)
     */
    @Transactional
    public ZReportDto closeRegister(
            String registerId, CloseCashRegisterRequest request, String userId) {
        String restaurantId = TenantContext.getRestaurantId();

        // 1. Fetch register
        CashRegister cashRegister =
                cashRegisterRepository
                        .findByIdAndRestaurantId(registerId, restaurantId)
                        .orElseThrow(() -> new NotFoundException("CashRegister", registerId));

        // 2. Validate register is open and belongs to user
        if (!cashRegister.isOpen()) {
            throw new ConflictException("Cash register is already closed");
        }

        if (!cashRegister.getUserId().equals(userId)) {
            throw new ConflictException("Cash register belongs to different user");
        }

        // 3. Calculate totals (INV-05)
        BigDecimal totalIncome = transactionRepository.sumIncomeByCashRegisterId(registerId);
        BigDecimal totalExpenses = transactionRepository.sumExpensesByCashRegisterId(registerId);
        long incomeCount = transactionRepository.countByCashRegisterIdAndType(registerId, "INCOME");
        long expenseCount =
                transactionRepository.countByCashRegisterIdAndType(registerId, "EXPENSE");

        // 4. Calculate expected amount (INV-05 invariant)
        BigDecimal expectedAmount =
                cashRegister.getOpeningAmount().add(totalIncome).subtract(totalExpenses);

        // 5. Calculate difference
        BigDecimal difference = request.getClosingAmount().subtract(expectedAmount);

        // 6. Build Z-report
        ZReportDto zReport =
                ZReportDto.builder()
                        .cashRegisterId(registerId)
                        .openingAmount(cashRegister.getOpeningAmount())
                        .expectedAmount(expectedAmount)
                        .declaredAmount(request.getClosingAmount())
                        .difference(difference)
                        .totalIncome(totalIncome)
                        .totalExpenses(totalExpenses)
                        .incomeCount(incomeCount)
                        .expenseCount(expenseCount)
                        .status(
                                difference.compareTo(BigDecimal.ZERO) == 0
                                        ? "BALANCED"
                                        : "DIFFERENCE")
                        .build();

        // 7. Update register status
        cashRegister.setClosingAmount(request.getClosingAmount());
        cashRegister.setStatus("CLOSED");
        cashRegister.setClosedAt(LocalDateTime.now());
        cashRegisterRepository.save(cashRegister);

        return zReport;
    }

    /** Get X-report (current status without closing). Partial report per SPEC-CASH-001. */
    @Transactional(readOnly = true)
    public XReportDto getXReport(String userId) {
        String restaurantId = TenantContext.getRestaurantId();

        // 1. Fetch open register for user
        CashRegister cashRegister =
                cashRegisterRepository
                        .findOpenByUserIdAndRestaurantId(userId, restaurantId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "No open cash register found for user"));

        // 2. Calculate totals
        BigDecimal totalIncome =
                transactionRepository.sumIncomeByCashRegisterId(cashRegister.getId());
        BigDecimal totalExpenses =
                transactionRepository.sumExpensesByCashRegisterId(cashRegister.getId());
        long incomeCount =
                transactionRepository.countByCashRegisterIdAndType(cashRegister.getId(), "INCOME");
        long expenseCount =
                transactionRepository.countByCashRegisterIdAndType(cashRegister.getId(), "EXPENSE");

        // 3. Calculate current balance
        BigDecimal currentBalance =
                cashRegister.getOpeningAmount().add(totalIncome).subtract(totalExpenses);

        // 4. Build X-report
        return XReportDto.builder()
                .cashRegisterId(cashRegister.getId())
                .currentBalance(currentBalance)
                .openingAmount(cashRegister.getOpeningAmount())
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .transactionCount(incomeCount + expenseCount)
                .incomeCount(incomeCount)
                .expenseCount(expenseCount)
                .build();
    }

    /** Get active cash register for current user. */
    @Transactional(readOnly = true)
    public CashRegisterDto getActiveRegister(String userId) {
        String restaurantId = TenantContext.getRestaurantId();

        CashRegister cashRegister =
                cashRegisterRepository
                        .findOpenByUserIdAndRestaurantId(userId, restaurantId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "No open cash register found for user"));

        return toDto(cashRegister);
    }

    /** List all registers for the restaurant. */
    @Transactional(readOnly = true)
    public List<CashRegisterDto> listRegisters() {
        String restaurantId = TenantContext.getRestaurantId();

        return cashRegisterRepository.findAllByRestaurantId(restaurantId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CashRegisterDto toDto(CashRegister cashRegister) {
        return CashRegisterDto.builder()
                .id(cashRegister.getId())
                .restaurantId(cashRegister.getRestaurantId())
                .userId(cashRegister.getUserId())
                .openingAmount(cashRegister.getOpeningAmount())
                .closingAmount(cashRegister.getClosingAmount())
                .status(cashRegister.getStatus())
                .openedAt(cashRegister.getOpenedAt())
                .closedAt(cashRegister.getClosedAt())
                .build();
    }
}
