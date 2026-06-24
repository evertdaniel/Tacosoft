import { describe, it, expect } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductsList } from './ProductsList';
import { productsFixture } from '@/test/fixtures';
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

describe('ProductsList', () => {
  it('renders a loading state', () => {
    render(<ProductsList />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading products/i)).toBeInTheDocument();
  });

  it('renders the list of products', async () => {
    render(<ProductsList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /products/i })).toBeInTheDocument());

    expect(screen.getAllByRole('listitem')).toHaveLength(productsFixture.length);
    expect(screen.getByText(productsFixture[0].name)).toBeInTheDocument();
  });

  it('renders an empty state when there are no products', async () => {
    server.use(
      http.get('http://localhost:8080/products', () => {
        return HttpResponse.json([]);
      })
    );

    render(<ProductsList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no products found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/products', () => {
        return new HttpResponse(JSON.stringify({ message: 'Products unavailable' }), { status: 500 });
      })
    );

    render(<ProductsList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Products unavailable/i);
  });
});
