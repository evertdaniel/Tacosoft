import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCreateCategory, useUpdateCategory, useDeleteCategory } from './useCategoryMutations';
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

describe('useCategoryMutations', () => {
  it('creates a category', async () => {
    const { result } = renderHook(() => useCreateCategory(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'New Category', sectionId: 'section-1' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('New Category');
  });

  it('updates a category', async () => {
    const { result } = renderHook(() => useUpdateCategory(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'category-1', body: { name: 'Updated Category' } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('Updated Category');
  });

  it('deletes a category', async () => {
    const { result } = renderHook(() => useDeleteCategory(), { wrapper: createWrapper() });

    result.current.mutate('category-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it('returns an error when create fails', async () => {
    server.use(
      http.post('http://localhost:8080/categories', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useCreateCategory(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'Bad Category', sectionId: 'section-1' });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Create failed');
  });
});
