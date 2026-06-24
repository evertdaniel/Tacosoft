import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProductReportView } from './ProductReportView';
import { productReportsFixture } from '@/test/fixtures';

describe('ProductReportView', () => {
  it('renders product report heading', () => {
    render(<ProductReportView reports={productReportsFixture} />);

    expect(screen.getByRole('heading', { name: /product report/i })).toBeInTheDocument();
  });

  it('renders each product row with revenue and margin', () => {
    render(<ProductReportView reports={productReportsFixture} />);

    const rows = screen.getAllByRole('row');
    expect(rows.length).toBeGreaterThan(1);

    expect(screen.getByText(productReportsFixture[0].productName)).toBeInTheDocument();
    expect(screen.getByText(productReportsFixture[1].productName)).toBeInTheDocument();
  });

  it('renders empty state when no products', () => {
    render(<ProductReportView reports={[]} />);

    expect(screen.getByText(/no products in this period/i)).toBeInTheDocument();
  });
});
