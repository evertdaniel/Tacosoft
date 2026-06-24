import { describe, it, expect } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import { FinancialReportView } from './FinancialReportView';
import { financialReportFixture } from '@/test/fixtures';

describe('FinancialReportView', () => {
  it('renders financial report heading and net cash flow', () => {
    render(<FinancialReportView report={financialReportFixture} />);

    expect(screen.getByRole('heading', { name: /financial report/i })).toBeInTheDocument();
    expect(screen.getByText(formatCurrency(financialReportFixture.netCashFlow))).toBeInTheDocument();
  });

  it('renders income and expense breakdowns', () => {
    render(<FinancialReportView report={financialReportFixture} />);

    const incomeSection = screen.getByRole('heading', { name: /income/i }).parentElement as HTMLElement;
    const expensesSection = screen.getByRole('heading', { name: /expenses/i }).parentElement as HTMLElement;

    expect(incomeSection).toBeInTheDocument();
    expect(expensesSection).toBeInTheDocument();
    expect(within(incomeSection).getByText(financialReportFixture.income[0].paymentMethod)).toBeInTheDocument();
    expect(within(expensesSection).getByText(financialReportFixture.expenses[0].paymentMethod)).toBeInTheDocument();
  });

  it('renders invoice summary', () => {
    render(<FinancialReportView report={financialReportFixture} />);

    expect(screen.getByRole('heading', { name: /invoice summary/i })).toBeInTheDocument();
    expect(screen.getByText(String(financialReportFixture.invoiceSummary.totalInvoices))).toBeInTheDocument();
  });
});

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}
