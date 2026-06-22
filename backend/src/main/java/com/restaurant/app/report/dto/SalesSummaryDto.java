package com.restaurant.app.report.dto;

import java.math.BigDecimal;
import java.util.List;

/** Sales summary with top products and period comparison. Implements SPEC-REPORT-001. */
public class SalesSummaryDto {

    private BigDecimal totalRevenue;
    private Long totalInvoices;
    private BigDecimal averageTicket;
    private List<TopProductDto> topProducts;
    private PeriodComparisonDto periodComparison;

    public SalesSummaryDto() {}

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(Long totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public BigDecimal getAverageTicket() {
        return averageTicket;
    }

    public void setAverageTicket(BigDecimal averageTicket) {
        this.averageTicket = averageTicket;
    }

    public List<TopProductDto> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductDto> topProducts) {
        this.topProducts = topProducts;
    }

    public PeriodComparisonDto getPeriodComparison() {
        return periodComparison;
    }

    public void setPeriodComparison(PeriodComparisonDto periodComparison) {
        this.periodComparison = periodComparison;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SalesSummaryDto dto = new SalesSummaryDto();

        public Builder totalRevenue(BigDecimal totalRevenue) {
            dto.totalRevenue = totalRevenue;
            return this;
        }

        public Builder totalInvoices(Long totalInvoices) {
            dto.totalInvoices = totalInvoices;
            return this;
        }

        public Builder averageTicket(BigDecimal averageTicket) {
            dto.averageTicket = averageTicket;
            return this;
        }

        public Builder topProducts(List<TopProductDto> topProducts) {
            dto.topProducts = topProducts;
            return this;
        }

        public Builder periodComparison(PeriodComparisonDto periodComparison) {
            dto.periodComparison = periodComparison;
            return this;
        }

        public SalesSummaryDto build() {
            return dto;
        }
    }
}
