import { describe, it, expect } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SectionsList } from './SectionsList';
import { sectionsFixture } from '@/test/fixtures';
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

describe('SectionsList', () => {
  it('renders a loading state', () => {
    render(<SectionsList />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading sections/i)).toBeInTheDocument();
  });

  it('renders the list of sections', async () => {
    render(<SectionsList />, { wrapper: createWrapper() });

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

    render(<SectionsList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no sections found/i)).toBeInTheDocument());
  });

  it('renders an error state when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/sections', () => {
        return new HttpResponse(JSON.stringify({ message: 'Sections unavailable' }), { status: 500 });
      })
    );

    render(<SectionsList />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());

    expect(screen.getByRole('alert')).toHaveTextContent(/Sections unavailable/i);
  });
});
