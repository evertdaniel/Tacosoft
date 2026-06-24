import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TableStatusBadge } from './TableStatusBadge';

describe('TableStatusBadge', () => {
  it.each([
    ['AVAILABLE', 'Available'],
    ['OCCUPIED', 'Occupied'],
    ['RESERVED', 'Reserved'],
    ['CLEANING', 'Cleaning'],
  ] as const)('renders %s as %s', (status, label) => {
    render(<TableStatusBadge status={status} />);

    expect(screen.getByText(label)).toBeInTheDocument();
  });
});
