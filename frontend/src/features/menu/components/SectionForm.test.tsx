import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SectionForm } from './SectionForm';
import type { SectionDto } from '@/types/domain.types';

describe('SectionForm', () => {
  it('renders empty fields for create', () => {
    render(<SectionForm onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('');
    expect(screen.getByLabelText(/display order/i)).toHaveValue(0);
  });

  it('renders initial data for edit', () => {
    const section: SectionDto = {
      id: 'section-1',
      restaurantId: 'rest-1',
      name: 'Food',
      description: 'Main section',
      displayOrder: 2,
      isActive: true,
    };

    render(<SectionForm initialData={section} onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('Food');
    expect(screen.getByLabelText(/description/i)).toHaveValue('Main section');
    expect(screen.getByLabelText(/display order/i)).toHaveValue(2);
  });

  it('calls onSubmit with form values', async () => {
    const handleSubmit = vi.fn();
    render(<SectionForm onSubmit={handleSubmit} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'Drinks');
    await userEvent.clear(screen.getByLabelText(/display order/i));
    await userEvent.type(screen.getByLabelText(/display order/i), '3');

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(handleSubmit).toHaveBeenCalledTimes(1));

    expect(handleSubmit).toHaveBeenCalledWith({
      name: 'Drinks',
      description: '',
      displayOrder: 3,
      isActive: true,
    });
  });

  it('shows validation error when name is empty', async () => {
    const handleSubmit = vi.fn();
    render(<SectionForm onSubmit={handleSubmit} />);

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.getByText(/name is required/i)).toBeInTheDocument());
    expect(handleSubmit).not.toHaveBeenCalled();
  });
});
