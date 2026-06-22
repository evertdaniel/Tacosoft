package com.restaurant.app.report.dto;

import java.math.BigDecimal;

/**
 * Invoice summary for financial report. Implements SPEC-REPORT-001 and requires judgment double
 * (💰).
 */
public class InvoiceSummaryDto {

    private Long totalInvoices;
    private Long paidInvoices;
    private Long unpaidInvoices;
    private BigDecimal totalInvoiced;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    private Double paymentRate;

    public InvoiceSummaryDto() {}

    public Long getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(Long totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public Long getPaidInvoices() {
        return paidInvoices;
    }

    public void setPaidInvoices(Long paidInvoices) {
        this.paidInvoices = paidInvoices;
    }

    public Long getUnpaidInvoices() {
        return unpaidInvoices;
    }

    public void setUnpaidInvoices(Long unpaidInvoices) {
        this.unpaidInvoices = unpaidInvoices;
    }

    public BigDecimal getTotalInvoiced() {
        return totalInvoiced;
    }

    public void setTotalInvoiced(BigDecimal totalInvoiced) {
        this.totalInvoiced = totalInvoiced;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(BigDecimal totalPending) {
        this.totalPending = totalPending;
    }

    public Double getPaymentRate() {
        return paymentRate;
    }

    public void setPaymentRate(Double paymentRate) {
        this.paymentRate = paymentRate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final InvoiceSummaryDto dto = new InvoiceSummaryDto();

        public Builder totalInvoices(Long totalInvoices) {
            dto.totalInvoices = totalInvoices;
            return this;
        }

        public Builder paidInvoices(Long paidInvoices) {
            dto.paidInvoices = paidInvoices;
            return this;
        }

        public Builder unpaidInvoices(Long unpaidInvoices) {
            dto.unpaidInvoices = unpaidInvoices;
            return this;
        }

        public Builder totalInvoiced(BigDecimal totalInvoiced) {
            dto.totalInvoiced = totalInvoiced;
            return this;
        }

        public Builder totalPaid(BigDecimal totalPaid) {
            dto.totalPaid = totalPaid;
            return this;
        }

        public Builder totalPending(BigDecimal totalPending) {
            dto.totalPending = totalPending;
            return this;
        }

        public Builder paymentRate(Double paymentRate) {
            dto.paymentRate = paymentRate;
            return this;
        }

        public InvoiceSummaryDto build() {
            return dto;
        }
    }
}
