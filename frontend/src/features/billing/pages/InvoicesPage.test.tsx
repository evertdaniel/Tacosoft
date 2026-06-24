import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { InvoicesPage } from './InvoicesPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { paymentBodyFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

function createRouter() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  const router = createMemoryRouter([{ path: '/billing', element: <InvoicesPage /> }], {
    initialEntries: ['/billing'],
  });

  return { queryClient, router };
}

function renderInvoicesPage() {
  const { queryClient, router } = createRouter();
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
  return { router };
}

describe('InvoicesPage', () => {
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

  it('renders the unpaid invoice list by default', async () => {
    renderInvoicesPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /billing/i })).toBeInTheDocument());

    expect(screen.getByText(/folio #1001/i)).toBeInTheDocument();
    expect(screen.queryByText(/folio #1002/i)).not.toBeInTheDocument();
  });

  it('switches to all invoices when toggled', async () => {
    renderInvoicesPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /billing/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /all invoices/i }));

    await waitFor(() => {
      expect(screen.getByText(/folio #1001/i)).toBeInTheDocument();
      expect(screen.getByText(/folio #1002/i)).toBeInTheDocument();
    });
  });

  it('opens payment modal when pay is clicked', async () => {
    renderInvoicesPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /billing/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /pay/i }));

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/record payment/i)).toBeInTheDocument();
  });

  it('submits payment and closes the modal', async () => {
    renderInvoicesPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /billing/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /pay/i }));

    await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument());

    await userEvent.selectOptions(screen.getByLabelText(/payment method/i), paymentBodyFixture.paymentMethod);
    await userEvent.click(screen.getByRole('button', { name: /confirm payment/i }));

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
  });

  it('shows empty state when no unpaid invoices exist', async () => {
    server.use(
      http.get('http://localhost:8080/invoices/unpaid', () => {
        return HttpResponse.json([]);
      })
    );

    renderInvoicesPage();

    await waitFor(() => expect(screen.getByText(/no unpaid invoices/i)).toBeInTheDocument());
  });

  it('shows error state when invoice loading fails', async () => {
    server.use(
      http.get('http://localhost:8080/invoices/unpaid', () => {
        return new HttpResponse(JSON.stringify({ message: 'Load failed' }), { status: 500 });
      })
    );

    renderInvoicesPage();

    await waitFor(() => expect(screen.getByText(/load failed/i)).toBeInTheDocument());
  });
});
