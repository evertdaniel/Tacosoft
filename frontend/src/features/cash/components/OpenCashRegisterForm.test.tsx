import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { OpenCashRegisterForm } from './OpenCashRegisterForm';

describe('OpenCashRegisterForm', () => {
  const onSubmit = vi.fn();
  const onCancel = vi.fn();

  beforeEach(() => {
    onSubmit.mockClear();
    onCancel.mockClear();
  });

  it('renders the opening amount input and buttons', () => {
    render(
      <OpenCashRegisterForm onSubmit={onSubmit} onCancel={onCancel} isSubmitting={false} />
    );

    expect(screen.getByLabelText(/initial amount/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /open register/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
  });

  it('submits the parsed amount', async () => {
    render(
      <OpenCashRegisterForm onSubmit={onSubmit} onCancel={onCancel} isSubmitting={false} />
    );

    await userEvent.clear(screen.getByLabelText(/initial amount/i));
    await userEvent.type(screen.getByLabelText(/initial amount/i), '25000');
    await userEvent.click(screen.getByRole('button', { name: /open register/i }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith({ openingAmount: 25000 }));
  });

  it('shows validation error for empty amount', async () => {
    render(
      <OpenCashRegisterForm onSubmit={onSubmit} onCancel={onCancel} isSubmitting={false} />
    );

    await userEvent.clear(screen.getByLabelText(/initial amount/i));
    await userEvent.click(screen.getByRole('button', { name: /open register/i }));

    await waitFor(() => expect(screen.getByText(/amount is required/i)).toBeInTheDocument());
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('shows validation error for negative amount', async () => {
    render(
      <OpenCashRegisterForm onSubmit={onSubmit} onCancel={onCancel} isSubmitting={false} />
    );

    await userEvent.clear(screen.getByLabelText(/initial amount/i));
    await userEvent.type(screen.getByLabelText(/initial amount/i), '-100');
    await userEvent.click(screen.getByRole('button', { name: /open register/i }));

    await waitFor(() => expect(screen.getByText(/amount must be zero or positive/i)).toBeInTheDocument());
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('calls onCancel when cancel is clicked', async () => {
    render(
      <OpenCashRegisterForm onSubmit={onSubmit} onCancel={onCancel} isSubmitting={false} />
    );

    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

    expect(onCancel).toHaveBeenCalledTimes(1);
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('disables inputs while submitting', () => {
    render(
      <OpenCashRegisterForm onSubmit={onSubmit} onCancel={onCancel} isSubmitting={true} />
    );

    expect(screen.getByLabelText(/initial amount/i)).toBeDisabled();
    expect(screen.getByRole('button', { name: /opening/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
  });
});
