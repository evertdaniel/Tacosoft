package com.restaurant.app.cash.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for CashRegisterService. SPEC-CASH-001, INV-CASH-001. */
@ExtendWith(MockitoExtension.class)
class CashRegisterServiceTest {

    @Mock private CashRegisterRepository cashRegisterRepository;

    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private CashRegisterService cashRegisterService;

    private final String restaurantId = "restaurant-1";
    private final String userId = "user-1";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void openRegister_NoOpenRegister_CreatesOpenRegister() {
        // Arrange
        OpenCashRegisterRequest request = new OpenCashRegisterRequest();
        request.setOpeningAmount(BigDecimal.valueOf(100));

        when(cashRegisterRepository.findAllOpenByRestaurantId(restaurantId)).thenReturn(List.of());
        when(cashRegisterRepository.save(any(CashRegister.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CashRegisterDto result = cashRegisterService.openRegister(request, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("OPEN");
        assertThat(result.getOpeningAmount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(result.getRestaurantId()).isEqualTo(restaurantId);
    }

    @Test
    void openRegister_RestaurantAlreadyHasOpenRegister_ThrowsConflictException() {
        // Arrange
        OpenCashRegisterRequest request = new OpenCashRegisterRequest();
        request.setOpeningAmount(BigDecimal.valueOf(100));

        CashRegister existingOpen =
                CashRegister.builder()
                        .id("register-1")
                        .restaurantId(restaurantId)
                        .userId("other-user")
                        .status("OPEN")
                        .build();

        when(cashRegisterRepository.findAllOpenByRestaurantId(restaurantId))
                .thenReturn(List.of(existingOpen));

        // Act & Assert
        assertThatThrownBy(() -> cashRegisterService.openRegister(request, userId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Restaurant already has an open cash register");

        verify(cashRegisterRepository, never()).save(any(CashRegister.class));
    }

    @Test
    void closeRegister_BalancedClosesRegisterAndReturnsZReport() {
        // Arrange
        CashRegister register = createOpenRegister();
        CloseCashRegisterRequest request = new CloseCashRegisterRequest();
        request.setClosingAmount(BigDecimal.valueOf(250));

        when(cashRegisterRepository.findByIdAndRestaurantId("register-1", restaurantId))
                .thenReturn(Optional.of(register));
        when(transactionRepository.sumIncomeByCashRegisterId("register-1"))
                .thenReturn(BigDecimal.valueOf(200));
        when(transactionRepository.sumExpensesByCashRegisterId("register-1"))
                .thenReturn(BigDecimal.valueOf(50));
        when(transactionRepository.countByCashRegisterIdAndType("register-1", "INCOME"))
                .thenReturn(3L);
        when(transactionRepository.countByCashRegisterIdAndType("register-1", "EXPENSE"))
                .thenReturn(1L);
        when(cashRegisterRepository.save(any(CashRegister.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ZReportDto result = cashRegisterService.closeRegister("register-1", request, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("BALANCED");
        assertThat(result.getExpectedAmount()).isEqualTo(BigDecimal.valueOf(250));
        assertThat(result.getDifference()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getTotalIncome()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(result.getTotalExpenses()).isEqualTo(BigDecimal.valueOf(50));
        assertThat(register.getStatus()).isEqualTo("CLOSED");
    }

    @Test
    void closeRegister_WithDifference_ReturnsDifferenceStatus() {
        // Arrange
        CashRegister register = createOpenRegister();
        CloseCashRegisterRequest request = new CloseCashRegisterRequest();
        request.setClosingAmount(BigDecimal.valueOf(260));

        when(cashRegisterRepository.findByIdAndRestaurantId("register-1", restaurantId))
                .thenReturn(Optional.of(register));
        when(transactionRepository.sumIncomeByCashRegisterId("register-1"))
                .thenReturn(BigDecimal.valueOf(200));
        when(transactionRepository.sumExpensesByCashRegisterId("register-1"))
                .thenReturn(BigDecimal.valueOf(50));
        when(transactionRepository.countByCashRegisterIdAndType("register-1", "INCOME"))
                .thenReturn(3L);
        when(transactionRepository.countByCashRegisterIdAndType("register-1", "EXPENSE"))
                .thenReturn(1L);
        when(cashRegisterRepository.save(any(CashRegister.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ZReportDto result = cashRegisterService.closeRegister("register-1", request, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("DIFFERENCE");
        assertThat(result.getDifference()).isEqualTo(BigDecimal.valueOf(10));
    }

    @Test
    void closeRegister_AlreadyClosed_ThrowsConflictException() {
        // Arrange
        CashRegister register = createOpenRegister();
        register.setStatus("CLOSED");

        when(cashRegisterRepository.findByIdAndRestaurantId("register-1", restaurantId))
                .thenReturn(Optional.of(register));

        // Act & Assert
        assertThatThrownBy(
                        () ->
                                cashRegisterService.closeRegister(
                                        "register-1", new CloseCashRegisterRequest(), userId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already closed");
    }

    @Test
    void closeRegister_DifferentUser_ThrowsConflictException() {
        // Arrange
        CashRegister register = createOpenRegister();
        register.setUserId("other-user");

        when(cashRegisterRepository.findByIdAndRestaurantId("register-1", restaurantId))
                .thenReturn(Optional.of(register));

        // Act & Assert
        assertThatThrownBy(
                        () ->
                                cashRegisterService.closeRegister(
                                        "register-1", new CloseCashRegisterRequest(), userId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("different user");
    }

    @Test
    void closeRegister_RegisterNotFound_ThrowsNotFoundException() {
        // Arrange
        when(cashRegisterRepository.findByIdAndRestaurantId("register-1", restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(
                        () ->
                                cashRegisterService.closeRegister(
                                        "register-1", new CloseCashRegisterRequest(), userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getXReport_OpenRegister_ReturnsCurrentBalance() {
        // Arrange
        CashRegister register = createOpenRegister();
        when(cashRegisterRepository.findOpenByUserIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(register));
        when(transactionRepository.sumIncomeByCashRegisterId("register-1"))
                .thenReturn(BigDecimal.valueOf(150));
        when(transactionRepository.sumExpensesByCashRegisterId("register-1"))
                .thenReturn(BigDecimal.valueOf(30));
        when(transactionRepository.countByCashRegisterIdAndType("register-1", "INCOME"))
                .thenReturn(2L);
        when(transactionRepository.countByCashRegisterIdAndType("register-1", "EXPENSE"))
                .thenReturn(1L);

        // Act
        XReportDto result = cashRegisterService.getXReport(userId);

        // Assert
        assertThat(result.getCurrentBalance()).isEqualTo(BigDecimal.valueOf(220)); // 100 + 150 - 30
        assertThat(result.getTransactionCount()).isEqualTo(3L);
    }

    @Test
    void getXReport_NoOpenRegister_ThrowsNotFoundException() {
        // Arrange
        when(cashRegisterRepository.findOpenByUserIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cashRegisterService.getXReport(userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getActiveRegister_OpenRegister_ReturnsRegister() {
        // Arrange
        CashRegister register = createOpenRegister();
        when(cashRegisterRepository.findOpenByUserIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(register));

        // Act
        CashRegisterDto result = cashRegisterService.getActiveRegister(userId);

        // Assert
        assertThat(result.getId()).isEqualTo("register-1");
        assertThat(result.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void listRegisters_ReturnsAllRegisters() {
        // Arrange
        CashRegister register = createOpenRegister();
        when(cashRegisterRepository.findAllByRestaurantId(restaurantId))
                .thenReturn(List.of(register));

        // Act
        List<CashRegisterDto> result = cashRegisterService.listRegisters();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("register-1");
    }

    // Helper methods

    private CashRegister createOpenRegister() {
        return CashRegister.builder()
                .id("register-1")
                .restaurantId(restaurantId)
                .userId(userId)
                .openingAmount(BigDecimal.valueOf(100))
                .status("OPEN")
                .build();
    }
}
