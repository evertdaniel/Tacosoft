package com.restaurant.app.cash.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.cash.dto.CashRegisterDto;
import com.restaurant.app.cash.dto.OpenCashRegisterRequest;
import com.restaurant.app.cash.model.CashRegister;
import com.restaurant.app.cash.repository.CashRegisterRepository;
import com.restaurant.app.cash.repository.TransactionRepository;
import com.restaurant.app.common.ConflictException;
import com.restaurant.app.security.TenantContext;
import java.math.BigDecimal;
import java.util.List;
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
}
