import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { KitchenOrderCard } from './KitchenOrderCard';
import { createOrderFixture, createOrderDetailFixture } from '@/test/fixtures';

describe('KitchenOrderCard', () => {
  const order = createOrderFixture('order-kitchen-1', 10, 'IN_PLACE', 'PENDING', [
    createOrderDetailFixture('detail-kitchen-1', 'product-1', 'Carne Asada Taco', 'PENDING', 2),
  ]);

  const item = { detail: order.details[0], order };

  const itemWithNotes = {
    detail: {
      ...order.details[0],
      id: 'detail-kitchen-2',
      notes: 'No onions',
      productOptionName: 'Extra Cheese',
    },
    order,
  };

  it('renders the production area name', () => {
    render(<KitchenOrderCard areaName="Kitchen" items={[item]} onUpdateStatus={() => {}} />);

    expect(screen.getByRole('heading', { name: 'Kitchen' })).toBeInTheDocument();
  });

  it('renders product name, order number, quantity and status', () => {
    render(<KitchenOrderCard areaName="Kitchen" items={[item]} onUpdateStatus={() => {}} />);

    expect(screen.getByText('Carne Asada Taco')).toBeInTheDocument();
    expect(screen.getByText(/order #10/i)).toBeInTheDocument();
    expect(screen.getByText('x2')).toBeInTheDocument();
    expect(screen.getByText('PENDING', { selector: 'span' })).toBeInTheDocument();
  });

  it('renders notes and product option when present', () => {
    render(<KitchenOrderCard areaName="Kitchen" items={[itemWithNotes]} onUpdateStatus={() => {}} />);

    expect(screen.getByText(/no onions/i)).toBeInTheDocument();
    expect(screen.getByText(/extra cheese/i)).toBeInTheDocument();
  });

  it('calls onUpdateStatus when a new status is selected and update is clicked', async () => {
    const handleUpdate = vi.fn();
    render(<KitchenOrderCard areaName="Kitchen" items={[item]} onUpdateStatus={handleUpdate} />);

    await userEvent.selectOptions(screen.getByLabelText('Status'), 'READY');
    await userEvent.click(screen.getByRole('button', { name: /update/i }));

    expect(handleUpdate).toHaveBeenCalledWith('detail-kitchen-1', 'READY');
  });

  it('disables the update button when the status has not changed', () => {
    render(<KitchenOrderCard areaName="Kitchen" items={[item]} onUpdateStatus={() => {}} />);

    expect(screen.getByRole('button', { name: /update/i })).toBeDisabled();
  });
});
