import { describe, expect, it, beforeEach } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { Shell } from './Shell';

function TestPage() {
  return <div data-testid="page-content">Dashboard Page</div>;
}

describe('Shell', () => {
  beforeEach(() => {
    resetAuthStore();
    resetTenantStore();
  });

  it('renders top bar, sidebar, main content and mobile nav', () => {
    useAuthStore.setState({
      token: 'token',
      user: { id: 'u1', username: 'admin', firstName: 'Admin', lastName: 'User', email: 'a@a.com', active: true, primaryRole: { id: 'r1', name: 'ADMIN' }, restaurantRoles: [] },
      currentRestaurant: { id: 'rest-1', name: 'Taqueria Principal' },
      expiresAt: null,
      isAuthenticated: true,
    });
    useTenantStore.setState({
      currentRestaurantId: 'rest-1',
      currentRole: { id: 'r1', name: 'ADMIN' },
      availableRoles: [],
    });

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<Shell />}>
            <Route path="/dashboard" element={<TestPage />} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(screen.getByLabelText('Sidebar navigation')).toBeInTheDocument();
    expect(screen.getByRole('navigation', { name: 'Mobile' })).toBeInTheDocument();
    expect(screen.getByTestId('page-content')).toHaveTextContent('Dashboard Page');
  });

  it('renders navigation filtered by the user role', () => {
    useAuthStore.setState({
      token: 'token',
      user: { id: 'u1', username: 'cook', firstName: 'Cook', lastName: 'User', email: 'c@a.com', active: true, primaryRole: { id: 'r2', name: 'COOK' }, restaurantRoles: [] },
      currentRestaurant: { id: 'rest-1', name: 'Taqueria Principal' },
      expiresAt: null,
      isAuthenticated: true,
    });
    useTenantStore.setState({
      currentRestaurantId: 'rest-1',
      currentRole: { id: 'r2', name: 'COOK' },
      availableRoles: [],
    });

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<Shell />}>
            <Route path="/dashboard" element={<TestPage />} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    const sidebar = screen.getByLabelText('Sidebar navigation');
    expect(within(sidebar).getByRole('link', { name: 'Orders' })).toBeInTheDocument();
    expect(within(sidebar).queryByRole('link', { name: 'Billing' })).not.toBeInTheDocument();
  });
});
