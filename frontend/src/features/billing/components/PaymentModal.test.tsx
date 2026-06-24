import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { PaymentModal } from './PaymentModal';
import { invoicesFixture } from '@/test/fixtures';

describe('PaymentModal', () => {
  it('does not render when closed', () => {
    render(
      <PaymentModal
        isOpen={false}
        invoice={invoicesFixture[0]}
        onClose={() => {}}
        onSubmit={() => {}}
        isSubmitting={false}
      />
    );

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('renders invoice total and payment form when open', () => {
    render(
      <PaymentModal
        isOpen={true}
        invoice={invoicesFixture[0]}
        onClose={() => {}}
        onSubmit={() => {}}
        isSubmitting={false}
      />
    );

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/record payment/i)).toBeInTheDocument();
    expect(screen.getByText(/folio #1001/i)).toBeInTheDocument();
  });

  it('submits payment with selected method and amount', async () => {
    const handleSubmit = vi.fn();
    render(
      <PaymentModal
        isOpen={true}
        invoice={invoicesFixture[0]}
        onClose={() => {}}
        onSubmit={handleSubmit}
        isSubmitting={false}
      />
    );

    await userEvent.selectOptions(screen.getByLabelText(/payment method/i), 'CASH');
    await userEvent.clear(screen.getByLabelText(/amount/i));
    await userEvent.type(screen.getByLabelText(/amount/i), '20230');
    await userEvent.click(screen.getByRole('button', { name: /confirm payment/i }));

    expect(handleSubmit).toHaveBeenCalledWith({
      amount: 20230,
      paymentMethod: 'CASH',
      referenceId: '',
    });
  });

  it('includes reference id when provided', async () => {
    const handleSubmit = vi.fn();
    render(
      <PaymentModal
        isOpen={true}
        invoice={invoicesFixture[0]}
        onClose={() => {}}
        onSubmit={handleSubmit}
        isSubmitting={false}
      />
    );

    await userEvent.selectOptions(screen.getByLabelText(/payment method/i), 'TRANSFER');
    await userEvent.type(screen.getByLabelText(/reference id/i), 'ref-456');
    await userEvent.click(screen.getByRole('button', { name: /confirm payment/i }));

    expect(handleSubmit).toHaveBeenCalledWith({
      amount: 20230,
      paymentMethod: 'TRANSFER',
      referenceId: 'ref-456',
    });
  });

  it('disables form and shows loading while submitting', () => {
    render(
      <PaymentModal
        isOpen={true}
        invoice={invoicesFixture[0]}
        onClose={() => {}}
        onSubmit={() => {}}
        isSubmitting={true}
      />
    );

    expect(screen.getByRole('button', { name: /processing/i })).toBeDisabled();
    expect(screen.getByLabelText(/payment method/i)).toBeDisabled();
    expect(screen.getByLabelText(/amount/i)).toBeDisabled();
  });

  it('calls onClose when cancel is clicked', async () => {
    const handleClose = vi.fn();
    render(
      <PaymentModal
        isOpen={true}
        invoice={invoicesFixture[0]}
        onClose={handleClose}
        onSubmit={() => {}}
        isSubmitting={false}
      />
    );

    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

    expect(handleClose).toHaveBeenCalled();
  });

  it('shows validation error when amount is missing', async () => {
    const handleSubmit = vi.fn();
    render(
      <PaymentModal
        isOpen={true}
        invoice={invoicesFixture[0]}
        onClose={() => {}}
        onSubmit={handleSubmit}
        isSubmitting={false}
      />
    );

    await userEvent.clear(screen.getByLabelText(/amount/i));
    await userEvent.click(screen.getByRole('button', { name: /confirm payment/i }));

    expect(screen.getByText(/amount is required/i)).toBeInTheDocument();
    expect(handleSubmit).not.toHaveBeenCalled();
  });
});
