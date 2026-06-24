import { describe, it, expect } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductOptionsList } from './ProductOptionsList';
import { productOptionsFixture } from '@/test/fixtures';
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

describe('ProductOptionsList', () => {
  it('renders a loading state', () => {
    render(<ProductOptionsList />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading options/i)).toBeInTheDocument();
  });

  it('renders the list of product options', async () => {
    render(<ProductOptionsList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /product options/i })).toBeInTheDocument());

    expect(screen.getAllByRole('listitem')).toHaveLength(productOptionsFixture.length);
    expect(screen.getByText(productOptionsFixture[0].name)).toBeInTheDocument();
  });

  it('renders an empty state when there are no product options', async () => {
    server.use(
      http.get('http://localhost:8080/product-options', () => {
        return HttpResponse.json([]);
      })
    );

    render(<ProductOptionsList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no options found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/product-options', () => {
        return new HttpResponse(JSON.stringify({ message: 'Options unavailable' }), { status: 500 });
      })
    );

    render(<ProductOptionsList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Options unavailable/i);
  });
});
