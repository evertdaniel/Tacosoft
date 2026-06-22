package com.restaurant.app.cash.dto;

import java.math.BigDecimal;

/** Z-report DTO - cash register closing report. Implements INV-05 (balance validation). */
public class ZReportDto {

    private String cashRegisterId;
    private BigDecimal openingAmount;
    private BigDecimal expectedAmount;
    private BigDecimal declaredAmount;
    private BigDecimal difference;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private long incomeCount;
    private long expenseCount;
    private String status; // BALANCED | DIFFERENCE

    public ZReportDto() {}

    public String getCashRegisterId() {
        return cashRegisterId;
    }

    public void setCashRegisterId(String cashRegisterId) {
        this.cashRegisterId = cashRegisterId;
    }

    public BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public void setOpeningAmount(BigDecimal openingAmount) {
        this.openingAmount = openingAmount;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public BigDecimal getDeclaredAmount() {
        return declaredAmount;
    }

    public void setDeclaredAmount(BigDecimal declaredAmount) {
        this.declaredAmount = declaredAmount;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ZReportDto dto = new ZReportDto();

        public Builder cashRegisterId(String cashRegisterId) {
            dto.setCashRegisterId(cashRegisterId);
            return this;
        }

        public Builder openingAmount(BigDecimal openingAmount) {
            dto.setOpeningAmount(openingAmount);
            return this;
        }

        public Builder expectedAmount(BigDecimal expectedAmount) {
            dto.setExpectedAmount(expectedAmount);
            return this;
        }

        public Builder declaredAmount(BigDecimal declaredAmount) {
            dto.setDeclaredAmount(declaredAmount);
            return this;
        }

        public Builder difference(BigDecimal difference) {
            dto.setDifference(difference);
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

        public Builder incomeCount(long incomeCount) {
            dto.setIncomeCount(incomeCount);
            return this;
        }

        public Builder expenseCount(long expenseCount) {
            dto.setExpenseCount(expenseCount);
            return this;
        }

        public Builder status(String status) {
            dto.setStatus(status);
            return this;
        }

        public ZReportDto build() {
            return dto;
        }
    }
}
