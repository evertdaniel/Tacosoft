import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { ReportsPage } from './ReportsPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import {
  salesSummaryFixture,
  productReportsFixture,
  financialReportFixture,
  footfallReportFixture,
  staffPlanningReportFixture,
} from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

function createRouter() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  const router = createMemoryRouter([{ path: '/reports', element: <ReportsPage /> }], {
    initialEntries: ['/reports'],
  });

  return { queryClient, router };
}

function renderReportsPage() {
  const { queryClient, router } = createRouter();
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
  return { router };
}

describe('ReportsPage', () => {
  beforeEach(() => {
    resetAuthStore();
    resetTenantStore();
    useAuthStore.setState({
      token: 'token',
      user: {
        id: 'u1',
        username: 'admin',
        firstName: 'Admin',
        lastName: 'User',
        email: 'a@a.com',
        active: true,
        primaryRole: { id: 'r1', name: 'ADMIN' },
        restaurantRoles: [],
      },
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

  it('renders heading and default date filter', () => {
    renderReportsPage();

    expect(screen.getByRole('heading', { name: /reports/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/start date/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/end date/i)).toBeInTheDocument();
  });

  it('loads sales, product and financial reports for the default range', async () => {
    renderReportsPage();

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /sales report/i })).toBeInTheDocument()
    );
    expect(screen.getByText(formatCurrency(salesSummaryFixture.totalRevenue))).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /products/i }));

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /product report/i })).toBeInTheDocument()
    );
    expect(screen.getByText(productReportsFixture[0].productName)).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /finances/i }));

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /financial report/i })).toBeInTheDocument()
    );
    expect(screen.getByText(formatCurrency(financialReportFixture.netCashFlow))).toBeInTheDocument();
  });

  it('shows error state when reports fail to load', async () => {
    server.use(
      http.get('http://localhost:8080/reports/sales', () => {
        return new HttpResponse(JSON.stringify({ message: 'Sales load failed' }), { status: 500 });
      })
    );

    renderReportsPage();

    await waitFor(() => expect(screen.getByText(/sales load failed/i)).toBeInTheDocument());
  });

  it('switches report tabs and renders selected report', async () => {
    renderReportsPage();

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /sales report/i })).toBeInTheDocument()
    );

    await userEvent.click(screen.getByRole('button', { name: /product/i }));

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /product report/i })).toBeInTheDocument()
    );
  });

  it('switches to footfall tab and renders footfall report', async () => {
    renderReportsPage();

    await userEvent.click(screen.getByRole('button', { name: /footfall/i }));

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /footfall report/i })).toBeInTheDocument()
    );
    expect(screen.getByText(`Date: ${footfallReportFixture.orderDate}`)).toBeInTheDocument();
  });

  it('switches to staff planning tab and renders staff planning report', async () => {
    renderReportsPage();

    await userEvent.click(screen.getByRole('button', { name: /staff/i }));

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: /staff planning/i })).toBeInTheDocument()
    );
    expect(screen.getByText(`Date: ${staffPlanningReportFixture.date}`)).toBeInTheDocument();
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
