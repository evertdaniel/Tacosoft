package com.restaurant.app.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sales report DTO with revenue by payment method and period comparison. Implements
 * SPEC-REPORT-001.
 */
public class SalesReportDto {

    private LocalDate saleDate;
    private String paymentMethod;
    private Long invoiceCount;
    private BigDecimal totalSubtotal;
    private BigDecimal totalTax;
    private BigDecimal totalRevenue;

    public SalesReportDto() {}

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getInvoiceCount() {
        return invoiceCount;
    }

    public void setInvoiceCount(Long invoiceCount) {
        this.invoiceCount = invoiceCount;
    }

    public BigDecimal getTotalSubtotal() {
        return totalSubtotal;
    }

    public void setTotalSubtotal(BigDecimal totalSubtotal) {
        this.totalSubtotal = totalSubtotal;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SalesReportDto dto = new SalesReportDto();

        public Builder saleDate(LocalDate saleDate) {
            dto.saleDate = saleDate;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            dto.paymentMethod = paymentMethod;
            return this;
        }

        public Builder invoiceCount(Long invoiceCount) {
            dto.invoiceCount = invoiceCount;
            return this;
        }

        public Builder totalSubtotal(BigDecimal totalSubtotal) {
            dto.totalSubtotal = totalSubtotal;
            return this;
        }

        public Builder totalTax(BigDecimal totalTax) {
            dto.totalTax = totalTax;
            return this;
        }

        public Builder totalRevenue(BigDecimal totalRevenue) {
            dto.totalRevenue = totalRevenue;
            return this;
        }

        public SalesReportDto build() {
            return dto;
        }
    }
}
