package com.restaurant.app.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Financial report with income/expense breakdown, cash register reconciliation, and invoice
 * summary. Implements SPEC-REPORT-001 and requires judgment double (💰).
 */
public class FinancialReportDto {

    private LocalDate transactionDate;
    private List<TransactionSummaryDto> income;
    private List<TransactionSummaryDto> expenses;
    private BigDecimal netCashFlow;
    private CashRegisterSummaryDto cashRegisterSummary;
    private InvoiceSummaryDto invoiceSummary;

    public FinancialReportDto() {}

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public List<TransactionSummaryDto> getIncome() {
        return income;
    }

    public void setIncome(List<TransactionSummaryDto> income) {
        this.income = income;
    }

    public List<TransactionSummaryDto> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<TransactionSummaryDto> expenses) {
        this.expenses = expenses;
    }

    public BigDecimal getNetCashFlow() {
        return netCashFlow;
    }

    public void setNetCashFlow(BigDecimal netCashFlow) {
        this.netCashFlow = netCashFlow;
    }

    public CashRegisterSummaryDto getCashRegisterSummary() {
        return cashRegisterSummary;
    }

    public void setCashRegisterSummary(CashRegisterSummaryDto cashRegisterSummary) {
        this.cashRegisterSummary = cashRegisterSummary;
    }

    public InvoiceSummaryDto getInvoiceSummary() {
        return invoiceSummary;
    }

    public void setInvoiceSummary(InvoiceSummaryDto invoiceSummary) {
        this.invoiceSummary = invoiceSummary;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FinancialReportDto dto = new FinancialReportDto();

        public Builder transactionDate(LocalDate transactionDate) {
            dto.transactionDate = transactionDate;
            return this;
        }

        public Builder income(List<TransactionSummaryDto> income) {
            dto.income = income;
            return this;
        }

        public Builder expenses(List<TransactionSummaryDto> expenses) {
            dto.expenses = expenses;
            return this;
        }

        public Builder netCashFlow(BigDecimal netCashFlow) {
            dto.netCashFlow = netCashFlow;
            return this;
        }

        public Builder cashRegisterSummary(CashRegisterSummaryDto cashRegisterSummary) {
            dto.cashRegisterSummary = cashRegisterSummary;
            return this;
        }

        public Builder invoiceSummary(InvoiceSummaryDto invoiceSummary) {
            dto.invoiceSummary = invoiceSummary;
            return this;
        }

        public FinancialReportDto build() {
            return dto;
        }
    }
}
