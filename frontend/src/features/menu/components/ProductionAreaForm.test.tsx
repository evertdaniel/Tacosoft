import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ProductionAreaForm } from './ProductionAreaForm';
import type { ProductionAreaDto } from '@/types/domain.types';

describe('ProductionAreaForm', () => {
  it('renders empty fields for create', () => {
    render(<ProductionAreaForm onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('');
  });

  it('renders initial data for edit', () => {
    const area: ProductionAreaDto = {
      id: 'area-1',
      name: 'Kitchen',
      description: 'Main kitchen',
      createdAt: '',
      updatedAt: '',
    };

    render(<ProductionAreaForm initialData={area} onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('Kitchen');
    expect(screen.getByLabelText(/description/i)).toHaveValue('Main kitchen');
  });

  it('calls onSubmit with form values', async () => {
    const handleSubmit = vi.fn();
    render(<ProductionAreaForm onSubmit={handleSubmit} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'Bar');

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(handleSubmit).toHaveBeenCalledTimes(1));

    expect(handleSubmit).toHaveBeenCalledWith({
      name: 'Bar',
      description: '',
    });
  });

  it('shows validation error when name is empty', async () => {
    const handleSubmit = vi.fn();
    render(<ProductionAreaForm onSubmit={handleSubmit} />);

    await userEvent.clear(screen.getByLabelText(/name/i));
    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.getByText(/name is required/i)).toBeInTheDocument());
    expect(handleSubmit).not.toHaveBeenCalled();
  });
});
