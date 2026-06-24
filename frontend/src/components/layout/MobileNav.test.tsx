import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { MobileNav } from './MobileNav';

describe('MobileNav', () => {
  it('renders navigation and uses responsive mobile classes', () => {
    const { container } = render(
      <MemoryRouter>
        <MobileNav role="ADMIN" />
      </MemoryRouter>
    );

    expect(container.firstChild).toHaveClass('flex');
    expect(container.firstChild).toHaveClass('lg:hidden');
    expect(screen.getByRole('navigation', { name: 'Mobile' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Dashboard' })).toBeInTheDocument();
  });

  it('filters items by role', () => {
    render(
      <MemoryRouter>
        <MobileNav role="WAITER" />
      </MemoryRouter>
    );

    expect(screen.getByRole('link', { name: 'Tables' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Orders' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Billing' })).not.toBeInTheDocument();
  });
});
