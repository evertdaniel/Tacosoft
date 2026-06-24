import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CategoriesList } from './CategoriesList';
import { categoriesFixture } from '@/test/fixtures';
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

describe('CategoriesList', () => {
  it('renders a loading state', () => {
    render(<CategoriesList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading categories/i)).toBeInTheDocument();
  });

  it('renders the list of categories', async () => {
    render(<CategoriesList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /categories/i })).toBeInTheDocument());

    expect(screen.getAllByRole('listitem')).toHaveLength(categoriesFixture.length);
    expect(screen.getByText(categoriesFixture[0].name)).toBeInTheDocument();
  });

  it('renders an empty state when there are no categories', async () => {
    server.use(
      http.get('http://localhost:8080/categories', () => {
        return HttpResponse.json([]);
      })
    );

    render(<CategoriesList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no categories found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/categories', () => {
        return new HttpResponse(JSON.stringify({ message: 'Categories unavailable' }), { status: 500 });
      })
    );

    render(<CategoriesList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Categories unavailable/i);
  });

  it('calls onAdd when the add button is clicked', async () => {
    const handleAdd = vi.fn();
    render(<CategoriesList onAdd={handleAdd} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /categories/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add category/i }));

    expect(handleAdd).toHaveBeenCalledTimes(1);
  });

  it('calls onEdit and onDelete for a category', async () => {
    const handleEdit = vi.fn();
    const handleDelete = vi.fn();
    render(<CategoriesList onAdd={() => {}} onEdit={handleEdit} onDelete={handleDelete} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getAllByRole('listitem')).toHaveLength(categoriesFixture.length));

    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);
    expect(handleEdit).toHaveBeenCalledWith(categoriesFixture[0]);

    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    expect(handleDelete).toHaveBeenCalledWith(categoriesFixture[0].id);
  });
});
