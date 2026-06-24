import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { InvoiceCard } from './InvoiceCard';
import { invoicesFixture } from '@/test/fixtures';
import { formatCurrency } from '@/utils/formatters';

describe('InvoiceCard', () => {
  it('renders folio, order reference and total', () => {
    render(<InvoiceCard invoice={invoicesFixture[0]} onPay={() => {}} />);

    expect(screen.getByText(/folio #1001/i)).toBeInTheDocument();
    expect(screen.getByText(/order #1/i)).toBeInTheDocument();
    expect(screen.getByText(formatCurrency(invoicesFixture[0].total))).toBeInTheDocument();
  });

  it('shows unpaid status for unpaid invoices', () => {
    render(<InvoiceCard invoice={invoicesFixture[0]} onPay={() => {}} />);

    expect(screen.getByText('Unpaid')).toBeInTheDocument();
  });

  it('shows paid status and payment method for paid invoices', () => {
    render(<InvoiceCard invoice={invoicesFixture[1]} onPay={() => {}} />);

    expect(screen.getByText(/paid/i)).toBeInTheDocument();
    expect(screen.getByText(/cash/i)).toBeInTheDocument();
  });

  it('shows pay button for unpaid invoices', () => {
    render(<InvoiceCard invoice={invoicesFixture[0]} onPay={() => {}} />);

    expect(screen.getByRole('button', { name: /pay/i })).toBeInTheDocument();
  });

  it('hides pay button for paid invoices', () => {
    render(<InvoiceCard invoice={invoicesFixture[1]} onPay={() => {}} />);

    expect(screen.queryByRole('button', { name: /pay/i })).not.toBeInTheDocument();
  });

  it('calls onPay when pay button is clicked', async () => {
    const handlePay = vi.fn();
    render(<InvoiceCard invoice={invoicesFixture[0]} onPay={handlePay} />);

    await userEvent.click(screen.getByRole('button', { name: /pay/i }));

    expect(handlePay).toHaveBeenCalledWith('invoice-1');
  });
});
