import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ProductForm } from './ProductForm';
import type { CategoryDto, ProductDto, ProductionAreaDto } from '@/types/domain.types';

const categories: CategoryDto[] = [
  { id: 'category-1', name: 'Tacos', description: '', sectionId: 'section-1', isActive: true, createdAt: '', updatedAt: '' },
];

const areas: ProductionAreaDto[] = [
  { id: 'area-1', name: 'Kitchen', description: '', createdAt: '', updatedAt: '' },
];

describe('ProductForm', () => {
  it('renders empty fields for create', () => {
    render(<ProductForm categories={categories} productionAreas={areas} onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('');
    expect(screen.getByLabelText(/price/i)).toHaveValue(0);
  });

  it('renders initial data for edit', () => {
    const product: ProductDto = {
      id: 'product-1',
      name: 'Carne Asada Taco',
      description: 'Grilled steak taco',
      price: 8500,
      categoryId: 'category-1',
      taxRate: 0.19,
      stock: 50,
      manageStock: true,
      status: 'AVAILABLE',
      imageUrl: null,
      preparationTime: 10,
      isActive: true,
      productionAreaId: 'area-1',
      createdAt: '',
      updatedAt: '',
    };

    render(<ProductForm categories={categories} productionAreas={areas} initialData={product} onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('Carne Asada Taco');
    expect(screen.getByLabelText(/price/i)).toHaveValue(8500);
  });

  it('calls onSubmit with form values', async () => {
    const handleSubmit = vi.fn();
    render(<ProductForm categories={categories} productionAreas={areas} onSubmit={handleSubmit} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'Chicken Taco');
    await userEvent.clear(screen.getByLabelText(/price/i));
    await userEvent.type(screen.getByLabelText(/price/i), '9000');

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(handleSubmit).toHaveBeenCalledTimes(1));

    expect(handleSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'Chicken Taco',
        price: 9000,
        categoryId: 'category-1',
      })
    );
  });

  it('shows validation error when required fields are empty', async () => {
    const handleSubmit = vi.fn();
    render(<ProductForm categories={categories} productionAreas={areas} onSubmit={handleSubmit} />);

    await userEvent.clear(screen.getByLabelText(/name/i));
    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.getByText(/name is required/i)).toBeInTheDocument());
    expect(handleSubmit).not.toHaveBeenCalled();
  });
});
