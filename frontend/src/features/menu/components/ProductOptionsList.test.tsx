import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductOptionsList } from './ProductOptionsList';
import { productOptionsFixture } from '@/test/fixtures';
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

describe('ProductOptionsList', () => {
  it('renders a loading state', () => {
    render(<ProductOptionsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading options/i)).toBeInTheDocument();
  });

  it('renders the list of product options', async () => {
    render(<ProductOptionsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

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

    render(<ProductOptionsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no options found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/product-options', () => {
        return new HttpResponse(JSON.stringify({ message: 'Options unavailable' }), { status: 500 });
      })
    );

    render(<ProductOptionsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Options unavailable/i);
  });

  it('calls onAdd when the add button is clicked', async () => {
    const handleAdd = vi.fn();
    render(<ProductOptionsList onAdd={handleAdd} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /product options/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add option/i }));

    expect(handleAdd).toHaveBeenCalledTimes(1);
  });

  it('calls onEdit and onDelete for an option', async () => {
    const handleEdit = vi.fn();
    const handleDelete = vi.fn();
    render(<ProductOptionsList onAdd={() => {}} onEdit={handleEdit} onDelete={handleDelete} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getAllByRole('listitem')).toHaveLength(productOptionsFixture.length));

    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);
    expect(handleEdit).toHaveBeenCalledWith(productOptionsFixture[0]);

    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    expect(handleDelete).toHaveBeenCalledWith(productOptionsFixture[0].id);
  });
});
