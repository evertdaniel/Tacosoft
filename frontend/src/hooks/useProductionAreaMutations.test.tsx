import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCreateProductionArea, useUpdateProductionArea, useDeleteProductionArea } from './useProductionAreaMutations';
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

describe('useProductionAreaMutations', () => {
  it('creates a production area', async () => {
    const { result } = renderHook(() => useCreateProductionArea(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'New Area' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('New Area');
  });

  it('updates a production area', async () => {
    const { result } = renderHook(() => useUpdateProductionArea(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'area-1', body: { name: 'Updated Area' } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('Updated Area');
  });

  it('deletes a production area', async () => {
    const { result } = renderHook(() => useDeleteProductionArea(), { wrapper: createWrapper() });

    result.current.mutate('area-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it('returns an error when create fails', async () => {
    server.use(
      http.post('http://localhost:8080/production-areas', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useCreateProductionArea(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'Bad Area' });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Create failed');
  });
});
