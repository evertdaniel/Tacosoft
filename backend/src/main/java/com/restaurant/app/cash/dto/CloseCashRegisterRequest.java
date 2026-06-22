package com.restaurant.app.cash.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Request DTO for closing a cash register with Z-report. Implements INV-05 (balance validation).
 */
public class CloseCashRegisterRequest {

    @NotNull(message = "closingAmount is required") @PositiveOrZero(message = "closingAmount must be zero or positive")
    private BigDecimal closingAmount;

    public CloseCashRegisterRequest() {}

    public BigDecimal getClosingAmount() {
        return closingAmount;
    }

    public void setClosingAmount(BigDecimal closingAmount) {
        this.closingAmount = closingAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CloseCashRegisterRequest request = new CloseCashRegisterRequest();

        public Builder closingAmount(BigDecimal closingAmount) {
            request.setClosingAmount(closingAmount);
            return this;
        }

        public CloseCashRegisterRequest build() {
            return request;
        }
    }
}
