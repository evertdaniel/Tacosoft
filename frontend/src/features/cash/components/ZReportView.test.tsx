import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ZReportView } from './ZReportView';
import { zReportFixture } from '@/test/fixtures';

describe('ZReportView', () => {
  it('renders the report title and status badge', () => {
    render(<ZReportView report={zReportFixture} />);

    expect(screen.getByRole('heading', { name: /z report/i })).toBeInTheDocument();
    expect(screen.getByText(/balanced/i)).toBeInTheDocument();
  });

  it('renders all report amounts', () => {
    render(<ZReportView report={zReportFixture} />);

    expect(screen.getByText(/opening amount/i)).toBeInTheDocument();
    expect(screen.getByText(/expected amount/i)).toBeInTheDocument();
    expect(screen.getByText(/declared amount/i)).toBeInTheDocument();
    expect(screen.getByText(/difference/i)).toBeInTheDocument();
  });

  it('renders transaction counts', () => {
    render(<ZReportView report={zReportFixture} />);

    expect(screen.getByText(String(zReportFixture.incomeCount))).toBeInTheDocument();
    expect(screen.getByText(String(zReportFixture.expenseCount))).toBeInTheDocument();
  });

  it('renders difference status for unbalanced report', async () => {
    const unbalanced = { ...zReportFixture, difference: -500, status: 'DIFFERENCE' as const };
    render(<ZReportView report={unbalanced} />);

    expect(screen.getAllByText('Difference')).toHaveLength(2);
    expect(screen.getByText('-$500.00')).toBeInTheDocument();
  });
});
