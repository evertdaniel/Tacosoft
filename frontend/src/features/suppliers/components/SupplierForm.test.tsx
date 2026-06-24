import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SupplierForm } from './SupplierForm';
import type { SupplierDto } from '@/types/domain.types';

describe('SupplierForm', () => {
  it('renders empty fields for create', () => {
    render(<SupplierForm onSubmit={() => {}} isLoading={false} />);

    expect(screen.getByLabelText('Name')).toHaveValue('');
    expect(screen.getByLabelText('Contact name')).toHaveValue('');
    expect(screen.getByLabelText('Email')).toHaveValue('');
  });

  it('renders initial data for edit', () => {
    const supplier: SupplierDto = {
      id: 'supplier-1',
      restaurantId: 'rest-1',
      name: 'Tortillas Del Norte',
      contactName: 'Juan Pérez',
      email: 'juan@tortillas.com',
      phone: '+56912345678',
      address: 'Av. Norte 123',
      taxId: '76.123.456-7',
      isActive: true,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
    };

    render(<SupplierForm initialData={supplier} onSubmit={() => {}} isLoading={false} />);

    expect(screen.getByLabelText('Name')).toHaveValue('Tortillas Del Norte');
    expect(screen.getByLabelText('Contact name')).toHaveValue('Juan Pérez');
    expect(screen.getByLabelText('Email')).toHaveValue('juan@tortillas.com');
  });

  it('calls onSubmit with form values for create', async () => {
    const handleSubmit = vi.fn();
    render(<SupplierForm onSubmit={handleSubmit} isLoading={false} />);

    await userEvent.type(screen.getByLabelText('Name'), 'New Supplier');
    await userEvent.type(screen.getByLabelText('Contact name'), 'Pedro López');
    await userEvent.type(screen.getByLabelText('Email'), 'pedro@newsupplier.com');
    await userEvent.type(screen.getByLabelText('Phone'), '+56911111111');
    await userEvent.type(screen.getByLabelText('Address'), 'Av. Central 789');
    await userEvent.type(screen.getByLabelText('Tax ID'), '76.111.111-1');

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(handleSubmit).toHaveBeenCalledTimes(1));

    expect(handleSubmit).toHaveBeenCalledWith({
      name: 'New Supplier',
      contactName: 'Pedro López',
      email: 'pedro@newsupplier.com',
      phone: '+56911111111',
      address: 'Av. Central 789',
      taxId: '76.111.111-1',
    });
  });

  it('shows validation error when name is empty', async () => {
    const handleSubmit = vi.fn();
    render(<SupplierForm onSubmit={handleSubmit} isLoading={false} />);

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.getByText(/name is required/i)).toBeInTheDocument());
    expect(handleSubmit).not.toHaveBeenCalled();
  });

  it('disables inputs and shows saving state while loading', () => {
    render(<SupplierForm onSubmit={() => {}} isLoading={true} />);

    expect(screen.getByLabelText('Name')).toBeDisabled();
    expect(screen.getByRole('button', { name: /saving/i })).toBeDisabled();
  });
});
