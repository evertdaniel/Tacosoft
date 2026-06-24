import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreateOrderForm } from './CreateOrderForm';
import { tablesFixture, productsFixture, productOptionsFixture } from '@/test/fixtures';

describe('CreateOrderForm', () => {
  it('renders type selector and people input', () => {
    render(
      <CreateOrderForm tables={tablesFixture} products={productsFixture} productOptions={productOptionsFixture} onSubmit={() => {}} />
    );

    expect(screen.getByLabelText(/order type/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/people/i)).toBeInTheDocument();
  });

  it('shows table selector for in-place orders', async () => {
    render(
      <CreateOrderForm tables={tablesFixture} products={productsFixture} productOptions={productOptionsFixture} onSubmit={() => {}} />
    );

    await userEvent.selectOptions(screen.getByLabelText(/order type/i), 'IN_PLACE');

    expect(screen.getByLabelText(/table/i)).toBeInTheDocument();
  });

  it('hides table selector for take-away orders', async () => {
    render(
      <CreateOrderForm tables={tablesFixture} products={productsFixture} productOptions={productOptionsFixture} onSubmit={() => {}} />
    );

    await userEvent.selectOptions(screen.getByLabelText(/order type/i), 'TAKE_AWAY');

    expect(screen.queryByLabelText(/table/i)).not.toBeInTheDocument();
  });

  it('adds a product line and submits the form', async () => {
    const handleSubmit = vi.fn();
    render(
      <CreateOrderForm tables={tablesFixture} products={productsFixture} productOptions={productOptionsFixture} onSubmit={handleSubmit} />
    );

    await userEvent.click(screen.getByRole('button', { name: /add product/i }));
    await userEvent.selectOptions(screen.getByLabelText(/product/i), productsFixture[0].id);
    await userEvent.clear(screen.getByLabelText(/quantity/i));
    await userEvent.type(screen.getByLabelText(/quantity/i), '2');

    await userEvent.click(screen.getByRole('button', { name: /create order/i }));

    expect(handleSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'IN_PLACE',
        people: 1,
        tableId: tablesFixture[0].id,
        details: expect.arrayContaining([
          expect.objectContaining({ productId: productsFixture[0].id, quantity: 2 }),
        ]),
      })
    );
  });

  it('calls onSubmit with take-away data', async () => {
    const handleSubmit = vi.fn();
    render(
      <CreateOrderForm tables={tablesFixture} products={productsFixture} productOptions={productOptionsFixture} onSubmit={handleSubmit} />
    );

    await userEvent.selectOptions(screen.getByLabelText(/order type/i), 'TAKE_AWAY');
    await userEvent.clear(screen.getByLabelText(/people/i));
    await userEvent.type(screen.getByLabelText(/people/i), '1');
    await userEvent.click(screen.getByRole('button', { name: /add product/i }));
    await userEvent.selectOptions(screen.getByLabelText(/product/i), productsFixture[1].id);

    await userEvent.click(screen.getByRole('button', { name: /create order/i }));

    expect(handleSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'TAKE_AWAY',
        people: 1,
        details: expect.arrayContaining([
          expect.objectContaining({ productId: productsFixture[1].id, quantity: 1 }),
        ]),
      })
    );
  });

  it('removes a product line when remove is clicked', async () => {
    render(
      <CreateOrderForm tables={tablesFixture} products={productsFixture} productOptions={productOptionsFixture} onSubmit={() => {}} />
    );

    await userEvent.click(screen.getByRole('button', { name: /add product/i }));
    expect(screen.getByLabelText(/product/i)).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /remove/i }));
    expect(screen.queryByLabelText(/product/i)).not.toBeInTheDocument();
  });

  it('shows validation error when no products are added', async () => {
    const handleSubmit = vi.fn();
    render(
      <CreateOrderForm tables={tablesFixture} products={productsFixture} productOptions={productOptionsFixture} onSubmit={handleSubmit} />
    );

    await userEvent.click(screen.getByRole('button', { name: /create order/i }));

    expect(screen.getByText(/at least one product/i)).toBeInTheDocument();
    expect(handleSubmit).not.toHaveBeenCalled();
  });
});
