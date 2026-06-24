import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { OrderCard } from './OrderCard';
import { ordersFixture } from '@/test/fixtures';
import { formatCurrency } from '@/utils/formatters';

describe('OrderCard', () => {
  it('renders order number, status and total', () => {
    render(<OrderCard order={ordersFixture[0]} onClick={() => {}} />);

    expect(screen.getByText(/order #1/i)).toBeInTheDocument();
    expect(screen.getByText('PENDING')).toBeInTheDocument();
    expect(screen.getByText(formatCurrency(ordersFixture[0].total))).toBeInTheDocument();
  });

  it('shows in-place label for in-place orders', () => {
    render(<OrderCard order={ordersFixture[0]} onClick={() => {}} />);

    expect(screen.getByText(/in place/i)).toBeInTheDocument();
  });

  it('shows take-away label for take-away orders', () => {
    render(<OrderCard order={ordersFixture[1]} onClick={() => {}} />);

    expect(screen.getByText(/take away/i)).toBeInTheDocument();
  });

  it('calls onClick when clicked', async () => {
    const handleClick = vi.fn();
    render(<OrderCard order={ordersFixture[0]} onClick={handleClick} />);

    await userEvent.click(screen.getByRole('button'));

    expect(handleClick).toHaveBeenCalledWith('order-1');
  });
});
