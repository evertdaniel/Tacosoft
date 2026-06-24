import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { SuppliersPage } from './SuppliersPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { suppliersFixture, createSupplierBodyFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

function createRouter() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  const router = createMemoryRouter([{ path: '/suppliers', element: <SuppliersPage /> }], {
    initialEntries: ['/suppliers'],
  });

  return { queryClient, router };
}

function renderSuppliersPage() {
  const { queryClient, router } = createRouter();
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
  return { router };
}

describe('SuppliersPage', () => {
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

  it('renders heading and supplier list', async () => {
    renderSuppliersPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /suppliers/i })).toBeInTheDocument());

    expect(screen.getByText(suppliersFixture[0].name)).toBeInTheDocument();
    expect(screen.getByText(suppliersFixture[1].name)).toBeInTheDocument();
  });

  it('filters suppliers when typing in search input', async () => {
    renderSuppliersPage();

    await waitFor(() => expect(screen.getByText(suppliersFixture[1].name)).toBeInTheDocument());

    await userEvent.type(screen.getByLabelText(/search suppliers/i), 'Tortillas');

    await waitFor(() => {
      expect(screen.getByText(suppliersFixture[0].name)).toBeInTheDocument();
      expect(screen.queryByText(suppliersFixture[1].name)).not.toBeInTheDocument();
    });
  });

  it('opens create supplier modal', async () => {
    renderSuppliersPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /suppliers/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add supplier/i }));

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/new supplier/i)).toBeInTheDocument();
  });

  it('creates a supplier and closes the modal', async () => {
    renderSuppliersPage();

    await waitFor(() => expect(screen.getByRole('heading', { name: /suppliers/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add supplier/i }));

    await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument());

    await userEvent.type(screen.getByLabelText('Name'), createSupplierBodyFixture.name);
    await userEvent.type(screen.getByLabelText('Contact name'), createSupplierBodyFixture.contactName ?? '');
    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
  });

  it('opens edit supplier modal with initial data', async () => {
    renderSuppliersPage();

    await waitFor(() => expect(screen.getByText(suppliersFixture[0].name)).toBeInTheDocument());

    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument());

    expect(screen.getByDisplayValue(suppliersFixture[0].name)).toBeInTheDocument();
  });

  it('deactivates an active supplier', async () => {
    renderSuppliersPage();

    await waitFor(() => expect(screen.getByText(suppliersFixture[0].name)).toBeInTheDocument());

    await userEvent.click(screen.getAllByRole('button', { name: 'Deactivate' })[0]);

    await waitFor(() =>
      expect(screen.getAllByTestId('toggle-supplier-button')[0]).toHaveTextContent('Activate')
    );
  });

  it('activates an inactive supplier', async () => {
    renderSuppliersPage();

    await waitFor(() => expect(screen.getByText(suppliersFixture[1].name)).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: 'Activate' }));

    await waitFor(() =>
      expect(screen.getAllByTestId('toggle-supplier-button')[1]).toHaveTextContent('Deactivate')
    );
  });

  it('shows empty state when no suppliers match search', async () => {
    renderSuppliersPage();

    await waitFor(() => expect(screen.getByText(suppliersFixture[0].name)).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('Search suppliers'), { target: { value: 'NoMatch' } });

    await waitFor(() => expect(screen.getByText(/no suppliers found/i)).toBeInTheDocument());
  });

  it('shows error state when suppliers fail to load', async () => {
    server.use(
      http.get('http://localhost:8080/suppliers', () => {
        return new HttpResponse(JSON.stringify({ message: 'Load failed' }), { status: 500 });
      })
    );

    renderSuppliersPage();

    await waitFor(() => expect(screen.getByText(/load failed/i)).toBeInTheDocument());
  });
});
