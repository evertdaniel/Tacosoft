import { describe, expect, it, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { RouterProvider, createMemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { routes } from './index';

function createTestRouter(initialEntry: string) {
  return createMemoryRouter(routes, {
    initialEntries: [initialEntry],
    future: {
      v7_startTransition: true,
      v7_relativeSplatPath: true,
    },
  });
}

function Providers({ children }: { children: React.ReactNode }) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}

describe('App router', () => {
  beforeEach(() => {
    resetAuthStore();
    resetTenantStore();
  });

  it('renders login page at /login', () => {
    const router = createTestRouter('/login');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('heading', { name: 'Sign in to Tacosoft' })).toBeInTheDocument();
  });

  it('renders the dashboard page for authenticated users', async () => {
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

    const router = createTestRouter('/dashboard');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
  });

  it('renders the tables page for authenticated users', async () => {
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

    const router = createTestRouter('/tables');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Tables' })).toBeInTheDocument();
  });

  it('renders the menu page for authenticated users', async () => {
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

    const router = createTestRouter('/menu');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Menu' })).toBeInTheDocument();
  });

  it('renders the orders page for authenticated users', async () => {
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

    const router = createTestRouter('/orders');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Orders' })).toBeInTheDocument();
  });

  it('renders the kitchen page for authenticated users', async () => {
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

    const router = createTestRouter('/kitchen');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Kitchen', level: 1 })).toBeInTheDocument();
  });

  it('renders the order detail page for authenticated users', async () => {
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

    const router = createTestRouter('/orders/order-1');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(await screen.findByText(/order #1/i)).toBeInTheDocument();
  });

  it('renders the billing page for authenticated users', async () => {
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

    const router = createTestRouter('/billing');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Billing' })).toBeInTheDocument();
  });

  it('renders the cash register page for authenticated users', async () => {
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

    const router = createTestRouter('/cash');
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Cash register' })).toBeInTheDocument();
  });

  it.each([
    ['/reports', 'Reports'],
    ['/suppliers', 'Suppliers'],
  ])('renders the %s placeholder', (path, label) => {
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

    const router = createTestRouter(path);
    render(
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    );

    expect(screen.getByText(`${label} page`)).toBeInTheDocument();
  });
});
