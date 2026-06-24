import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { OrderDetailItem } from './OrderDetailItem';
import { orderDetailFixture, orderDetailReadyFixture } from '@/test/fixtures';

describe('OrderDetailItem', () => {
  it('renders product name, quantity and status', () => {
    render(<OrderDetailItem detail={orderDetailFixture} onUpdateStatus={() => {}} />);

    expect(screen.getByText('Carne Asada Taco')).toBeInTheDocument();
    expect(screen.getByText(/x2/i)).toBeInTheDocument();
    expect(screen.getByText('PENDING')).toBeInTheDocument();
  });

  it('renders option name when present', () => {
    const detail = { ...orderDetailFixture, productOptionName: 'Extra Cheese' };
    render(<OrderDetailItem detail={detail} onUpdateStatus={() => {}} />);

    expect(screen.getByText(/extra cheese/i)).toBeInTheDocument();
  });

  it('calls onUpdateStatus with the selected status', async () => {
    const handleUpdate = vi.fn();
    render(<OrderDetailItem detail={orderDetailFixture} onUpdateStatus={handleUpdate} />);

    await userEvent.selectOptions(screen.getByLabelText(/status/i), 'READY');
    await userEvent.click(screen.getByRole('button', { name: /update/i }));

    expect(handleUpdate).toHaveBeenCalledWith('detail-1', 'READY');
  });

  it('disables update when selected status equals current status', () => {
    render(<OrderDetailItem detail={orderDetailReadyFixture} onUpdateStatus={() => {}} />);

    expect(screen.getByRole('button', { name: /update/i })).toBeDisabled();
  });
});
