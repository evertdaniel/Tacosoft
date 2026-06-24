import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { RoleNav } from './RoleNav';

describe('RoleNav', () => {
  it('renders navigation links for ADMIN', () => {
    render(
      <MemoryRouter>
        <RoleNav role="ADMIN" />
      </MemoryRouter>
    );

    expect(screen.getByRole('link', { name: 'Dashboard' })).toHaveAttribute('href', '/dashboard');
    expect(screen.getByRole('link', { name: 'Billing' })).toHaveAttribute('href', '/billing');
    expect(screen.getByRole('link', { name: 'Suppliers' })).toHaveAttribute('href', '/suppliers');
  });

  it('renders kitchen items and hides billing for COOK', () => {
    render(
      <MemoryRouter>
        <RoleNav role="COOK" />
      </MemoryRouter>
    );

    expect(screen.getByRole('link', { name: 'Orders' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Billing' })).not.toBeInTheDocument();
  });

  it('marks the active route link', () => {
    render(
      <MemoryRouter initialEntries={['/orders']}>
        <RoleNav role="WAITER" />
      </MemoryRouter>
    );

    const ordersLink = screen.getByRole('link', { name: 'Orders' });
    expect(ordersLink).toHaveAttribute('aria-current', 'page');
  });
});
