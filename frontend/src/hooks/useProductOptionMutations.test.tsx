import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCreateProductOption, useUpdateProductOption, useDeleteProductOption } from './useProductOptionMutations';
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

describe('useProductOptionMutations', () => {
  it('creates a product option', async () => {
    const { result } = renderHook(() => useCreateProductOption(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'New Option', priceAdjustment: 500, productId: 'product-1' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('New Option');
  });

  it('updates a product option', async () => {
    const { result } = renderHook(() => useUpdateProductOption(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'option-1', body: { name: 'Updated Option' } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('Updated Option');
  });

  it('deletes a product option', async () => {
    const { result } = renderHook(() => useDeleteProductOption(), { wrapper: createWrapper() });

    result.current.mutate('option-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it('returns an error when create fails', async () => {
    server.use(
      http.post('http://localhost:8080/product-options', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useCreateProductOption(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'Bad Option', priceAdjustment: 0, productId: 'product-1' });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Create failed');
  });
});
