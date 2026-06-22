package com.restaurant.app.report.dto;

import java.math.BigDecimal;

/** Transaction summary by payment method. Implements SPEC-REPORT-001. */
public class TransactionSummaryDto {

    private String paymentMethod;
    private Long transactionCount;
    private BigDecimal totalAmount;

    public TransactionSummaryDto() {}

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final TransactionSummaryDto dto = new TransactionSummaryDto();

        public Builder paymentMethod(String paymentMethod) {
            dto.paymentMethod = paymentMethod;
            return this;
        }

        public Builder transactionCount(Long transactionCount) {
            dto.transactionCount = transactionCount;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            dto.totalAmount = totalAmount;
            return this;
        }

        public TransactionSummaryDto build() {
            return dto;
        }
    }
}
