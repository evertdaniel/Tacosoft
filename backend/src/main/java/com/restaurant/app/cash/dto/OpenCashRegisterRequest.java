package com.restaurant.app.cash.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** Request DTO for opening a cash register. */
public class OpenCashRegisterRequest {

    @NotNull(message = "openingAmount is required") @PositiveOrZero(message = "openingAmount must be zero or positive")
    private BigDecimal openingAmount;

    public OpenCashRegisterRequest() {}

    public BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public void setOpeningAmount(BigDecimal openingAmount) {
        this.openingAmount = openingAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final OpenCashRegisterRequest request = new OpenCashRegisterRequest();

        public Builder openingAmount(BigDecimal openingAmount) {
            request.setOpeningAmount(openingAmount);
            return this;
        }

        public OpenCashRegisterRequest build() {
            return request;
        }
    }
}
