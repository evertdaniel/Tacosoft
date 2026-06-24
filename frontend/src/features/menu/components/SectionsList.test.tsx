import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SectionsList } from './SectionsList';
import { sectionsFixture } from '@/test/fixtures';
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

describe('SectionsList', () => {
  it('renders a loading state', () => {
    render(<SectionsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading sections/i)).toBeInTheDocument();
  });

  it('renders the list of sections', async () => {
    render(<SectionsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /sections/i })).toBeInTheDocument());

    expect(screen.getAllByRole('listitem')).toHaveLength(sectionsFixture.length);
    expect(screen.getByText(sectionsFixture[0].name)).toBeInTheDocument();
  });

  it('renders an empty state when there are no sections', async () => {
    server.use(
      http.get('http://localhost:8080/sections', () => {
        return HttpResponse.json([]);
      })
    );

    render(<SectionsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no sections found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/sections', () => {
        return new HttpResponse(JSON.stringify({ message: 'Sections unavailable' }), { status: 500 });
      })
    );

    render(<SectionsList onAdd={() => {}} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Sections unavailable/i);
  });

  it('calls onAdd when the add button is clicked', async () => {
    const handleAdd = vi.fn();
    render(<SectionsList onAdd={handleAdd} onEdit={() => {}} onDelete={() => {}} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('heading', { name: /sections/i })).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: /add section/i }));

    expect(handleAdd).toHaveBeenCalledTimes(1);
  });

  it('calls onEdit and onDelete for a section', async () => {
    const handleEdit = vi.fn();
    const handleDelete = vi.fn();
    render(<SectionsList onAdd={() => {}} onEdit={handleEdit} onDelete={handleDelete} />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getAllByRole('listitem')).toHaveLength(sectionsFixture.length));

    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);
    expect(handleEdit).toHaveBeenCalledWith(sectionsFixture[0]);

    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    expect(handleDelete).toHaveBeenCalledWith(sectionsFixture[0].id);
  });
});
