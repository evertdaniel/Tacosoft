import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CategoryForm } from './CategoryForm';
import type { CategoryDto, SectionDto } from '@/types/domain.types';

const sections: SectionDto[] = [
  { id: 'section-1', restaurantId: 'rest-1', name: 'Food', description: '', displayOrder: 1, isActive: true },
];

describe('CategoryForm', () => {
  it('renders empty fields for create', () => {
    render(<CategoryForm sections={sections} onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('');
    expect(screen.getByLabelText(/section/i)).toHaveValue('section-1');
  });

  it('renders initial data for edit', () => {
    const category: CategoryDto = {
      id: 'category-1',
      name: 'Tacos',
      description: 'Mexican tacos',
      sectionId: 'section-1',
      isActive: true,
      createdAt: '',
      updatedAt: '',
    };

    render(<CategoryForm sections={sections} initialData={category} onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('Tacos');
    expect(screen.queryByLabelText(/section/i)).not.toBeInTheDocument();
  });

  it('calls onSubmit with form values', async () => {
    const handleSubmit = vi.fn();
    render(<CategoryForm sections={sections} onSubmit={handleSubmit} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'Burritos');

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(handleSubmit).toHaveBeenCalledTimes(1));

    expect(handleSubmit).toHaveBeenCalledWith({
      name: 'Burritos',
      description: '',
      sectionId: 'section-1',
    });
  });

  it('shows validation error when name is empty', async () => {
    const handleSubmit = vi.fn();
    render(<CategoryForm sections={sections} onSubmit={handleSubmit} />);

    await userEvent.clear(screen.getByLabelText(/name/i));
    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.getByText(/name is required/i)).toBeInTheDocument());
    expect(handleSubmit).not.toHaveBeenCalled();
  });
});
