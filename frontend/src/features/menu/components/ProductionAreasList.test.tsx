import { describe, it, expect } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductionAreasList } from './ProductionAreasList';
import { productionAreasFixture } from '@/test/fixtures';
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

describe('ProductionAreasList', () => {
  it('renders a loading state', () => {
    render(<ProductionAreasList />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading production areas/i)).toBeInTheDocument();
  });

  it('renders the list of production areas', async () => {
    render(<ProductionAreasList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /production areas/i })).toBeInTheDocument());

    expect(screen.getAllByRole('listitem')).toHaveLength(productionAreasFixture.length);
    expect(screen.getByText(productionAreasFixture[0].name)).toBeInTheDocument();
  });

  it('renders an empty state when there are no production areas', async () => {
    server.use(
      http.get('http://localhost:8080/production-areas', () => {
        return HttpResponse.json([]);
      })
    );

    render(<ProductionAreasList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no production areas found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/production-areas', () => {
        return new HttpResponse(JSON.stringify({ message: 'Production areas unavailable' }), { status: 500 });
      })
    );

    render(<ProductionAreasList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Production areas unavailable/i);
  });
});
