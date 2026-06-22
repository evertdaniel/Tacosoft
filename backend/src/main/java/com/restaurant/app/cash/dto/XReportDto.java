package com.restaurant.app.cash.dto;

import java.math.BigDecimal;

/**
 * X-report DTO - current cash register status without closing. Partial report per SPEC-CASH-001.
 */
public class XReportDto {

    private String cashRegisterId;
    private BigDecimal currentBalance;
    private BigDecimal openingAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private long transactionCount;
    private long incomeCount;
    private long expenseCount;

    public XReportDto() {}

    public String getCashRegisterId() {
        return cashRegisterId;
    }

    public void setCashRegisterId(String cashRegisterId) {
        this.cashRegisterId = cashRegisterId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public void setOpeningAmount(BigDecimal openingAmount) {
        this.openingAmount = openingAmount;
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

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public long getIncomeCount() {
        return incomeCount;
    }

    public void setIncomeCount(long incomeCount) {
        this.incomeCount = incomeCount;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(long expenseCount) {
        this.expenseCount = expenseCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final XReportDto dto = new XReportDto();

        public Builder cashRegisterId(String cashRegisterId) {
            dto.setCashRegisterId(cashRegisterId);
            return this;
        }

        public Builder currentBalance(BigDecimal currentBalance) {
            dto.setCurrentBalance(currentBalance);
            return this;
        }

        public Builder openingAmount(BigDecimal openingAmount) {
            dto.setOpeningAmount(openingAmount);
            return this;
        }

        public Builder totalIncome(BigDecimal totalIncome) {
            dto.setTotalIncome(totalIncome);
            return this;
        }

        public Builder totalExpenses(BigDecimal totalExpenses) {
            dto.setTotalExpenses(totalExpenses);
            return this;
        }

        public Builder transactionCount(long transactionCount) {
            dto.setTransactionCount(transactionCount);
            return this;
        }

        public Builder incomeCount(long incomeCount) {
            dto.setIncomeCount(incomeCount);
            return this;
        }

        public Builder expenseCount(long expenseCount) {
            dto.setExpenseCount(expenseCount);
            return this;
        }

        public XReportDto build() {
            return dto;
        }
    }
}
