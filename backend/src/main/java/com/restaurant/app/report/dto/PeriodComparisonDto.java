package com.restaurant.app.report.dto;

import java.math.BigDecimal;

/** Period comparison for sales report (current vs previous period). Implements SPEC-REPORT-001. */
public class PeriodComparisonDto {

    private BigDecimal currentRevenue;
    private BigDecimal previousRevenue;
    private BigDecimal growth;
    private Double growthPercentage;

    public PeriodComparisonDto() {}

    public BigDecimal getCurrentRevenue() {
        return currentRevenue;
    }

    public void setCurrentRevenue(BigDecimal currentRevenue) {
        this.currentRevenue = currentRevenue;
    }

    public BigDecimal getPreviousRevenue() {
        return previousRevenue;
    }

    public void setPreviousRevenue(BigDecimal previousRevenue) {
        this.previousRevenue = previousRevenue;
    }

    public BigDecimal getGrowth() {
        return growth;
    }

    public void setGrowth(BigDecimal growth) {
        this.growth = growth;
    }

    public Double getGrowthPercentage() {
        return growthPercentage;
    }

    public void setGrowthPercentage(Double growthPercentage) {
        this.growthPercentage = growthPercentage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PeriodComparisonDto dto = new PeriodComparisonDto();

        public Builder currentRevenue(BigDecimal currentRevenue) {
            dto.currentRevenue = currentRevenue;
            return this;
        }

        public Builder previousRevenue(BigDecimal previousRevenue) {
            dto.previousRevenue = previousRevenue;
            return this;
        }

        public Builder growth(BigDecimal growth) {
            dto.growth = growth;
            return this;
        }

        public Builder growthPercentage(Double growthPercentage) {
            dto.growthPercentage = growthPercentage;
            return this;
        }

        public PeriodComparisonDto build() {
            return dto;
        }
    }
}
