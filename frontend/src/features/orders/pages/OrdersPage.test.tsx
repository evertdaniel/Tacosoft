import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { OrdersPage } from './OrdersPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { productsFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

function createRouter() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  const router = createMemoryRouter([{ path: '/orders', element: <OrdersPage /> }], {
    initialEntries: ['/orders'],
  });

  return { queryClient, router };
}

function renderOrdersPage() {
  const { queryClient, router } = createRouter();
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
  return { router };
}

describe('OrdersPage', () => {
  beforeEach(() => {
    resetAuthStore();
    resetTenantStore();
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
  });

  it('renders the order list', async () => {
    renderOrdersPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /orders/i })).toBeInTheDocument());

    expect(screen.getByText(/order #1/i)).toBeInTheDocument();
    expect(screen.getByText(/order #2/i)).toBeInTheDocument();
  });

  it('opens the create order modal when Add Order is clicked', async () => {
    renderOrdersPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /orders/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add order/i }));

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /create order/i })).toBeInTheDocument();
  });

  it('submits the create order form and closes the modal', async () => {
    renderOrdersPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /orders/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add order/i }));
    await userEvent.click(screen.getByRole('button', { name: /add product/i }));
    await userEvent.selectOptions(screen.getByLabelText(/product/i), productsFixture[0].id);
    await userEvent.click(screen.getByRole('button', { name: /create order/i }));

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
  });

  it('shows an error message when order creation fails', async () => {
    server.use(
      http.post('http://localhost:8080/orders', () => {
        return new HttpResponse(JSON.stringify({ message: 'Order create failed' }), { status: 500 });
      })
    );

    renderOrdersPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /orders/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add order/i }));
    await userEvent.click(screen.getByRole('button', { name: /add product/i }));
    await userEvent.click(screen.getByRole('button', { name: /create order/i }));

    await waitFor(() => expect(screen.getByText(/order create failed/i)).toBeInTheDocument());
  });

  it('shows empty state when no orders exist', async () => {
    server.use(
      http.get('http://localhost:8080/orders', () => {
        return HttpResponse.json([]);
      })
    );

    renderOrdersPage();

    await waitFor(() => expect(screen.getByText(/no orders found/i)).toBeInTheDocument());
  });
});
