package com.restaurant.app.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Request DTO for recording payments. Implements INV-03 (idempotent payments). */
public class PaymentRequest {

    @NotNull(message = "amount is required") @Positive(message = "amount must be positive") private BigDecimal amount;

    @NotNull(message = "paymentMethod is required") private String paymentMethod; // CASH | CREDIT_CARD | TRANSFER

    private String referenceId; // Optional client reference

    public PaymentRequest() {}

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PaymentRequest request = new PaymentRequest();

        public Builder amount(BigDecimal amount) {
            request.setAmount(amount);
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            request.setPaymentMethod(paymentMethod);
            return this;
        }

        public Builder referenceId(String referenceId) {
            request.setReferenceId(referenceId);
            return this;
        }

        public PaymentRequest build() {
            return request;
        }
    }
}
