import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { OrderDetailPage } from './OrderDetailPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { ordersFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

function renderOrderDetailPage(orderId = 'order-1') {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  const router = createMemoryRouter(
    [{ path: '/orders/:id', element: <OrderDetailPage /> }],
    { initialEntries: [`/orders/${orderId}`] }
  );

  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );

  return { router };
}

describe('OrderDetailPage', () => {
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

  it('renders order details', async () => {
    renderOrderDetailPage('order-1');

    await waitFor(() => expect(screen.getByText(/order #1/i)).toBeInTheDocument());

    expect(screen.getAllByText('PENDING')[0]).toBeInTheDocument();
    expect(screen.getByText('Carne Asada Taco')).toBeInTheDocument();
  });

  it('updates a detail status', async () => {
    renderOrderDetailPage('order-1');

    await waitFor(() => expect(screen.getByText('Carne Asada Taco')).toBeInTheDocument());

    await userEvent.selectOptions(screen.getByLabelText(/status/i), 'READY');
    await userEvent.click(screen.getByRole('button', { name: /update/i }));

    await waitFor(() => expect(screen.getByText(/updated/i)).toBeInTheDocument());
  });

  it('navigates back when Back is clicked', async () => {
    const { router } = renderOrderDetailPage('order-1');

    await waitFor(() => expect(screen.getByText(/order #1/i)).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /back/i }));

    expect(router.state.location.pathname).toBe('/orders');
  });

  it('shows error state when order fetch fails', async () => {
    server.use(
      http.get('http://localhost:8080/orders/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Order not found' }), { status: 404 });
      })
    );

    renderOrderDetailPage('order-1');

    await waitFor(() => expect(screen.getByText(/order not found/i)).toBeInTheDocument());
  });
});
