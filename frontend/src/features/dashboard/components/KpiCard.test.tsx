import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { KpiCard } from './KpiCard';

function TestIcon({ className }: { className?: string }) {
  return (
    <svg className={className} data-testid="test-icon" aria-hidden="true" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="10" />
    </svg>
  );
}

describe('KpiCard', () => {
  it('renders the label and formatted value', () => {
    render(<KpiCard label="Occupied Tables" value="8 / 15" />);

    expect(screen.getByText('Occupied Tables')).toBeInTheDocument();
    expect(screen.getByText('8 / 15')).toBeInTheDocument();
  });

  it('renders an icon when provided', () => {
    render(<KpiCard label="Sales Today" value="$1,234.50" icon={TestIcon} />);

    expect(screen.getByTestId('test-icon')).toBeInTheDocument();
  });

  it('renders without an icon', () => {
    render(<KpiCard label="Low Stock" value="3" />);

    expect(screen.queryByTestId('test-icon')).not.toBeInTheDocument();
  });
});
