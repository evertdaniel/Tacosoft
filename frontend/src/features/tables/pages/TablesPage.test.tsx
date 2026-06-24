import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import userEvent from '@testing-library/user-event';
import { TablesPage } from './TablesPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';
import { tablesFixture } from '@/test/fixtures';

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  return function Wrapper({ children }: { children: React.ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe('TablesPage', () => {
  beforeEach(() => {
    resetAuthStore();
    resetTenantStore();
    useAuthStore.setState({
      token: 'token',
      user: null,
      currentRestaurant: null,
      expiresAt: null,
      isAuthenticated: true,
    });
    useTenantStore.setState({
      currentRestaurantId: 'rest-1',
      currentRole: null,
      availableRoles: [],
    });
  });

  it('renders a loading state then the table grid', async () => {
    render(<TablesPage />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading tables/i)).toBeInTheDocument();

    await waitFor(() => expect(screen.getByRole('heading', { name: /tables/i })).toBeInTheDocument());

    expect(screen.getAllByTestId('table-card')).toHaveLength(tablesFixture.length);
  });

  it('updates a table status and refreshes the grid', async () => {
    render(<TablesPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getAllByTestId('table-card')).toHaveLength(tablesFixture.length));

    const card = screen.getAllByTestId('table-card')[1];
    const select = card.querySelector('select') as HTMLSelectElement;
    const updateButton = card.querySelector('button') as HTMLButtonElement;

    await userEvent.selectOptions(select, 'AVAILABLE');
    await userEvent.click(updateButton);

    await waitFor(() => expect(screen.getAllByText('Available').length).toBeGreaterThanOrEqual(1));
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/tables', () => {
        return new HttpResponse(JSON.stringify({ message: 'Tables unavailable' }), { status: 500 });
      })
    );

    render(<TablesPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Tables unavailable/i);
  });
});
