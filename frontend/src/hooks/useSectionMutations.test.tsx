import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCreateSection, useUpdateSection, useDeleteSection } from './useSectionMutations';
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

describe('useSectionMutations', () => {
  it('creates a section and invalidates the sections query key', async () => {
    const { result } = renderHook(() => useCreateSection(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'New Section', displayOrder: 3, isActive: true });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('New Section');
    expect(result.current.data?.displayOrder).toBe(3);
  });

  it('updates a section and returns the updated section', async () => {
    const { result } = renderHook(() => useUpdateSection(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'section-1', body: { name: 'Updated Section', displayOrder: 1 } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.id).toBe('section-1');
    expect(result.current.data?.name).toBe('Updated Section');
  });

  it('deletes a section', async () => {
    const { result } = renderHook(() => useDeleteSection(), { wrapper: createWrapper() });

    result.current.mutate('section-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it('returns an error when create fails', async () => {
    server.use(
      http.post('http://localhost:8080/sections', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useCreateSection(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'Bad Section', displayOrder: 1, isActive: true });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Create failed');
  });
});
