import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { XReportView } from './XReportView';
import { xReportFixture } from '@/test/fixtures';

describe('XReportView', () => {
  it('renders the report title and register id', () => {
    render(<XReportView report={xReportFixture} />);

    expect(screen.getByRole('heading', { name: /x report/i })).toBeInTheDocument();
    expect(screen.getByText(`Register: ${xReportFixture.cashRegisterId}`)).toBeInTheDocument();
  });

  it('renders all report amounts', () => {
    render(<XReportView report={xReportFixture} />);

    expect(screen.getByText(/current balance/i)).toBeInTheDocument();
    expect(screen.getByText(/opening amount/i)).toBeInTheDocument();
    expect(screen.getByText(/total income/i)).toBeInTheDocument();
    expect(screen.getByText(/total expenses/i)).toBeInTheDocument();
  });

  it('renders transaction counts', () => {
    render(<XReportView report={xReportFixture} />);

    expect(screen.getByText(String(xReportFixture.transactionCount))).toBeInTheDocument();
    expect(screen.getByText(String(xReportFixture.incomeCount))).toBeInTheDocument();
    expect(screen.getByText(String(xReportFixture.expenseCount))).toBeInTheDocument();
  });
});
