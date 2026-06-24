import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ProductOptionForm } from './ProductOptionForm';
import type { ProductDto, ProductOptionDto } from '@/types/domain.types';

const products: ProductDto[] = [
  {
    id: 'product-1',
    name: 'Carne Asada Taco',
    description: '',
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
  },
];

describe('ProductOptionForm', () => {
  it('renders empty fields for create', () => {
    render(<ProductOptionForm products={products} onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('');
    expect(screen.getByLabelText(/price adjustment/i)).toHaveValue(0);
  });

  it('renders initial data for edit', () => {
    const option: ProductOptionDto = {
      id: 'option-1',
      name: 'Extra Cheese',
      description: 'Add cheese',
      priceAdjustment: 1500,
      productId: 'product-1',
      isDefault: false,
      isAvailable: true,
      createdAt: '',
      updatedAt: '',
    };

    render(<ProductOptionForm products={products} initialData={option} onSubmit={() => {}} />);

    expect(screen.getByLabelText(/name/i)).toHaveValue('Extra Cheese');
    expect(screen.getByLabelText(/price adjustment/i)).toHaveValue(1500);
  });

  it('calls onSubmit with form values', async () => {
    const handleSubmit = vi.fn();
    render(<ProductOptionForm products={products} onSubmit={handleSubmit} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'Guacamole');
    await userEvent.clear(screen.getByLabelText(/price adjustment/i));
    await userEvent.type(screen.getByLabelText(/price adjustment/i), '500');

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(handleSubmit).toHaveBeenCalledTimes(1));

    expect(handleSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'Guacamole',
        priceAdjustment: 500,
        productId: 'product-1',
      })
    );
  });

  it('shows validation error when required fields are empty', async () => {
    const handleSubmit = vi.fn();
    render(<ProductOptionForm products={products} onSubmit={handleSubmit} />);

    await userEvent.clear(screen.getByLabelText(/name/i));
    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.getByText(/name is required/i)).toBeInTheDocument());
    expect(handleSubmit).not.toHaveBeenCalled();
  });
});
