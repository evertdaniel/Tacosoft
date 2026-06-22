package com.restaurant.app.report.dto;

import java.math.BigDecimal;

/**
 * Cash register reconciliation summary. Implements SPEC-REPORT-001 and requires judgment double
 * (💰).
 */
public class CashRegisterSummaryDto {

    private Integer openRegisters;
    private Integer closedRegisters;
    private BigDecimal totalOpeningBalance;
    private BigDecimal totalClosingBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal expectedBalance;
    private BigDecimal actualBalance;
    private BigDecimal discrepancy;

    public CashRegisterSummaryDto() {}

    public Integer getOpenRegisters() {
        return openRegisters;
    }

    public void setOpenRegisters(Integer openRegisters) {
        this.openRegisters = openRegisters;
    }

    public Integer getClosedRegisters() {
        return closedRegisters;
    }

    public void setClosedRegisters(Integer closedRegisters) {
        this.closedRegisters = closedRegisters;
    }

    public BigDecimal getTotalOpeningBalance() {
        return totalOpeningBalance;
    }

    public void setTotalOpeningBalance(BigDecimal totalOpeningBalance) {
        this.totalOpeningBalance = totalOpeningBalance;
    }

    public BigDecimal getTotalClosingBalance() {
        return totalClosingBalance;
    }

    public void setTotalClosingBalance(BigDecimal totalClosingBalance) {
        this.totalClosingBalance = totalClosingBalance;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getExpectedBalance() {
        return expectedBalance;
    }

    public void setExpectedBalance(BigDecimal expectedBalance) {
        this.expectedBalance = expectedBalance;
    }

    public BigDecimal getActualBalance() {
        return actualBalance;
    }

    public void setActualBalance(BigDecimal actualBalance) {
        this.actualBalance = actualBalance;
    }

    public BigDecimal getDiscrepancy() {
        return discrepancy;
    }

    public void setDiscrepancy(BigDecimal discrepancy) {
        this.discrepancy = discrepancy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CashRegisterSummaryDto dto = new CashRegisterSummaryDto();

        public Builder openRegisters(Integer openRegisters) {
            dto.openRegisters = openRegisters;
            return this;
        }

        public Builder closedRegisters(Integer closedRegisters) {
            dto.closedRegisters = closedRegisters;
            return this;
        }

        public Builder totalOpeningBalance(BigDecimal totalOpeningBalance) {
            dto.totalOpeningBalance = totalOpeningBalance;
            return this;
        }

        public Builder totalClosingBalance(BigDecimal totalClosingBalance) {
            dto.totalClosingBalance = totalClosingBalance;
            return this;
        }

        public Builder totalIncome(BigDecimal totalIncome) {
            dto.totalIncome = totalIncome;
            return this;
        }

        public Builder totalExpenses(BigDecimal totalExpenses) {
            dto.totalExpenses = totalExpenses;
            return this;
        }

        public Builder expectedBalance(BigDecimal expectedBalance) {
            dto.expectedBalance = expectedBalance;
            return this;
        }

        public Builder actualBalance(BigDecimal actualBalance) {
            dto.actualBalance = actualBalance;
            return this;
        }

        public Builder discrepancy(BigDecimal discrepancy) {
            dto.discrepancy = discrepancy;
            return this;
        }

        public CashRegisterSummaryDto build() {
            return dto;
        }
    }
}
