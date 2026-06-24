import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { CashRegisterPage } from './CashRegisterPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { xReportFixture, zReportFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

function createRouter() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  const router = createMemoryRouter([{ path: '/cash', element: <CashRegisterPage /> }], {
    initialEntries: ['/cash'],
  });

  return { queryClient, router };
}

function renderCashPage() {
  const { queryClient, router } = createRouter();
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
  return { router };
}

describe('CashRegisterPage', () => {
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

  it('renders the open register form when no active register exists', async () => {
    server.use(
      http.get('http://localhost:8080/cash-registers/active', () => {
        return new HttpResponse(JSON.stringify({ message: 'No active register' }), { status: 404 });
      })
    );

    renderCashPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /cash register/i })).toBeInTheDocument());
    expect(screen.getByLabelText(/initial amount/i)).toBeInTheDocument();
  });

  it('renders active register info and X report', async () => {
    renderCashPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /cash register/i })).toBeInTheDocument());

    expect(screen.getByRole('heading', { name: /active register/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /x report/i })).toBeInTheDocument();
    expect(within(screen.getByTestId('x-report-view')).getByText(formatCurrency(xReportFixture.currentBalance))).toBeInTheDocument();
  });

  it('opens and submits the close register form', async () => {
    renderCashPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /cash register/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /close register/i }));

    await waitFor(() => expect(screen.getByLabelText(/closing amount/i)).toBeInTheDocument());

    await userEvent.clear(screen.getByLabelText(/closing amount/i));
    await userEvent.type(screen.getByLabelText(/closing amount/i), '75000');
    await userEvent.click(screen.getByRole('button', { name: /confirm close/i }));

    await waitFor(() => expect(screen.getByRole('heading', { name: /z report/i })).toBeInTheDocument());
    expect(within(screen.getByTestId('z-report-view')).getByText(`Register: ${zReportFixture.cashRegisterId}`)).toBeInTheDocument();
  });

  it('shows the latest Z report section', async () => {
    renderCashPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /cash register/i })).toBeInTheDocument());
    await waitFor(() => expect(screen.getByRole('heading', { name: /z report/i })).toBeInTheDocument());
    expect(within(screen.getByTestId('z-report-view')).getByText(`Register: ${zReportFixture.cashRegisterId}`)).toBeInTheDocument();
  });

  it('shows error state when active register fails unexpectedly', async () => {
    server.use(
      http.get('http://localhost:8080/cash-registers/active', () => {
        return new HttpResponse(JSON.stringify({ message: 'Load failed' }), { status: 500 });
      })
    );

    renderCashPage();

    await waitFor(() => expect(screen.getByText(/load failed/i)).toBeInTheDocument());
  });
});

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}
