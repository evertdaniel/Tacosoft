import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductionAreasList } from './ProductionAreasList';
import { productionAreasFixture } from '@/test/fixtures';
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

describe('ProductionAreasList', () => {
  it('renders a loading state', () => {
    render(<ProductionAreasList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading production areas/i)).toBeInTheDocument();
  });

  it('renders the list of production areas', async () => {
    render(<ProductionAreasList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

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

    render(<ProductionAreasList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no production areas found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/production-areas', () => {
        return new HttpResponse(JSON.stringify({ message: 'Production areas unavailable' }), { status: 500 });
      })
    );

    render(<ProductionAreasList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Production areas unavailable/i);
  });

  it('calls onAdd when the add button is clicked', async () => {
    const handleAdd = vi.fn();
    render(<ProductionAreasList onAdd={handleAdd} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /production areas/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add production area/i }));

    expect(handleAdd).toHaveBeenCalledTimes(1);
  });

  it('calls onEdit and onDelete for a production area', async () => {
    const handleEdit = vi.fn();
    const handleDelete = vi.fn();
    render(<ProductionAreasList onAdd={() => {}} onEdit={handleEdit} onDelete={handleDelete} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getAllByRole('listitem')).toHaveLength(productionAreasFixture.length));

    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);
    expect(handleEdit).toHaveBeenCalledWith(productionAreasFixture[0]);

    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    expect(handleDelete).toHaveBeenCalledWith(productionAreasFixture[0].id);
  });
});
