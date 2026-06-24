import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SalesReportView } from './SalesReportView';
import { salesSummaryFixture } from '@/test/fixtures';

describe('SalesReportView', () => {
  it('renders sales summary totals', () => {
    render(<SalesReportView report={salesSummaryFixture} />);

    expect(screen.getByRole('heading', { name: /sales report/i })).toBeInTheDocument();
    expect(screen.getByText(formatCurrency(salesSummaryFixture.totalRevenue))).toBeInTheDocument();
    expect(screen.getByText(String(salesSummaryFixture.totalInvoices))).toBeInTheDocument();
  });

  it('renders top products list', () => {
    render(<SalesReportView report={salesSummaryFixture} />);

    expect(screen.getByRole('heading', { name: /top products/i })).toBeInTheDocument();
    expect(screen.getByText(salesSummaryFixture.topProducts[0].productName)).toBeInTheDocument();
    expect(screen.getByText(salesSummaryFixture.topProducts[1].productName)).toBeInTheDocument();
  });

  it('renders period comparison', () => {
    render(<SalesReportView report={salesSummaryFixture} />);

    expect(screen.getByText(/period comparison/i)).toBeInTheDocument();
    expect(screen.getByText(formatCurrency(salesSummaryFixture.periodComparison.growth))).toBeInTheDocument();
    expect(
      screen.getByText(`${salesSummaryFixture.periodComparison.growthPercentage?.toFixed(2)}%`)
    ).toBeInTheDocument();
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
