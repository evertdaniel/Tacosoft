import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Sidebar } from './Sidebar';

describe('Sidebar', () => {
  it('renders navigation and uses responsive desktop classes', () => {
    const { container } = render(
      <MemoryRouter>
        <Sidebar role="ADMIN" />
      </MemoryRouter>
    );

    expect(container.firstChild).toHaveClass('hidden');
    expect(container.firstChild).toHaveClass('lg:flex');
    expect(screen.getByRole('navigation', { name: 'Main' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Dashboard' })).toBeInTheDocument();
  });

  it('filters items by role', () => {
    render(
      <MemoryRouter>
        <Sidebar role="COOK" />
      </MemoryRouter>
    );

    expect(screen.getByRole('link', { name: 'Orders' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Billing' })).not.toBeInTheDocument();
  });
});
