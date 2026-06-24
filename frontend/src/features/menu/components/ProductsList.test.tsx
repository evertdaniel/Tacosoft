import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductsList } from './ProductsList';
import { productsFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';
import userEvent from '@testing-library/user-event';

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
    render(<ProductsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading products/i)).toBeInTheDocument();
  });

  it('renders the list of products', async () => {
    render(<ProductsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

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

    render(<ProductsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no products found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/products', () => {
        return new HttpResponse(JSON.stringify({ message: 'Products unavailable' }), { status: 500 });
      })
    );

    render(<ProductsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Products unavailable/i);
  });

  it('calls onAdd when the add button is clicked', async () => {
    const handleAdd = vi.fn();
    render(<ProductsList onAdd={handleAdd} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /products/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add product/i }));

    expect(handleAdd).toHaveBeenCalledTimes(1);
  });

  it('calls onEdit and onDelete for a product', async () => {
    const handleEdit = vi.fn();
    const handleDelete = vi.fn();
    render(<ProductsList onAdd={() => {}} onEdit={handleEdit} onDelete={handleDelete} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getAllByRole('listitem')).toHaveLength(productsFixture.length));

    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);
    expect(handleEdit).toHaveBeenCalledWith(productsFixture[0]);

    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    expect(handleDelete).toHaveBeenCalledWith(productsFixture[0].id);
  });
});
