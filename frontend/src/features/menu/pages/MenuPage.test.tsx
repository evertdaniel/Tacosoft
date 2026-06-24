import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import userEvent from '@testing-library/user-event';
import { MenuPage } from './MenuPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { categoriesFixture, sectionsFixture } from '@/test/fixtures';

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

describe('MenuPage', () => {
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

  it('renders the sections tab by default', async () => {
    render(<MenuPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /sections/i })).toBeInTheDocument());

    expect(screen.getByText(sectionsFixture[0].name)).toBeInTheDocument();
  });

  it('switches to the categories tab when clicked', async () => {
    render(<MenuPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /sections/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('tab', { name: /categories/i }));

    await waitFor(() => expect(screen.getByRole('heading', { name: /categories/i })).toBeInTheDocument());

    expect(screen.getByText(categoriesFixture[0].name)).toBeInTheDocument();
    expect(screen.queryByText(sectionsFixture[0].name)).not.toBeInTheDocument();
  });

  it('switches to the products tab when clicked', async () => {
    render(<MenuPage />, { wrapper: createWrapper() });

    await userEvent.click(screen.getByRole('tab', { name: /products/i }));

    await waitFor(() => expect(screen.getByRole('heading', { name: /products/i })).toBeInTheDocument());
  });
});
