import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import userEvent from '@testing-library/user-event';
import { MenuPage } from './MenuPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { categoriesFixture, sectionsFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

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

  it('opens the section create modal when Add Section is clicked', async () => {
    render(<MenuPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /sections/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add section/i }));

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/create section/i)).toBeInTheDocument();
  });

  it('submits the section form and closes the modal', async () => {
    render(<MenuPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /sections/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add section/i }));

    await userEvent.type(screen.getByLabelText(/name/i), 'Drinks');
    await userEvent.clear(screen.getByLabelText(/display order/i));
    await userEvent.type(screen.getByLabelText(/display order/i), '3');

    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
  });

  it('opens the edit modal when Edit is clicked on a section', async () => {
    render(<MenuPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getAllByRole('listitem')).toHaveLength(sectionsFixture.length));

    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByDisplayValue(sectionsFixture[0].name)).toBeInTheDocument();
  });

  it('deletes a section when Delete is clicked', async () => {
    render(<MenuPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getAllByRole('listitem')).toHaveLength(sectionsFixture.length));

    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
  });

  it('shows an error message when the section create fails', async () => {
    server.use(
      http.post('http://localhost:8080/sections', () => {
        return new HttpResponse(JSON.stringify({ message: 'Section create failed' }), { status: 500 });
      })
    );

    render(<MenuPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /sections/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add section/i }));

    await userEvent.type(screen.getByLabelText(/name/i), 'Bad Section');
    await userEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => expect(screen.getByText(/section create failed/i)).toBeInTheDocument());
  });

  it('renders the production areas tab and opens its create modal', async () => {
    render(<MenuPage />, { wrapper: createWrapper() });

    await userEvent.click(screen.getByRole('tab', { name: /production areas/i }));

    await waitFor(() => expect(screen.getByRole('heading', { name: /production areas/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add production area/i }));

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/create production area/i)).toBeInTheDocument();
  });
});
