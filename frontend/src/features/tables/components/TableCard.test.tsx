import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TableCard } from './TableCard';
import { createTableFixture } from '@/test/fixtures';

describe('TableCard', () => {
  it('renders table number, seats and current status', () => {
    const table = createTableFixture('table-1', 1, 'AVAILABLE', 4);

    render(<TableCard table={table} onUpdate={vi.fn()} />);

    expect(screen.getByRole('heading', { name: /table 1/i })).toBeInTheDocument();
    expect(screen.getByText(/4 seats/i)).toBeInTheDocument();
    expect(screen.getByText('Available')).toBeInTheDocument();
  });

  it('calls onUpdate when a new status is selected and update is clicked', async () => {
    const table = createTableFixture('table-1', 1, 'OCCUPIED', 2);
    const onUpdate = vi.fn();

    render(<TableCard table={table} onUpdate={onUpdate} />);

    await userEvent.selectOptions(screen.getByLabelText(/status/i), 'AVAILABLE');
    await userEvent.click(screen.getByRole('button', { name: /update/i }));

    expect(onUpdate).toHaveBeenCalledWith('table-1', 'AVAILABLE');
  });

  it('does not call onUpdate when the same status is selected', async () => {
    const table = createTableFixture('table-1', 1, 'AVAILABLE', 4);
    const onUpdate = vi.fn();

    render(<TableCard table={table} onUpdate={onUpdate} />);

    await userEvent.selectOptions(screen.getByLabelText(/status/i), 'AVAILABLE');
    await userEvent.click(screen.getByRole('button', { name: /update/i }));

    expect(onUpdate).not.toHaveBeenCalled();
  });
});
