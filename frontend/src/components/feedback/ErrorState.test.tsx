import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ErrorState } from './ErrorState';

describe('ErrorState', () => {
  it('renders the title and message', () => {
    render(<ErrorState title="Request failed" message="Unable to load data." />);
    expect(screen.getByRole('alert')).toHaveTextContent('Request failed');
    expect(screen.getByText('Unable to load data.')).toBeInTheDocument();
  });

  it('renders a retry button that calls onRetry when clicked', async () => {
    const onRetry = vi.fn();
    render(<ErrorState title="Oops" message="Try again" onRetry={onRetry} />);

    await userEvent.click(screen.getByRole('button', { name: 'Retry' }));
    expect(onRetry).toHaveBeenCalledTimes(1);
  });

  it('does not render retry button when onRetry is omitted', () => {
    render(<ErrorState title="Oops" message="No retry available" />);
    expect(screen.queryByRole('button', { name: 'Retry' })).not.toBeInTheDocument();
  });
});
