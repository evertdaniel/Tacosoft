import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCreateProduct, useUpdateProduct, useDeleteProduct } from './useProductMutations';
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

describe('useProductMutations', () => {
  it('creates a product', async () => {
    const { result } = renderHook(() => useCreateProduct(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'New Product', price: 12000, categoryId: 'category-1' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('New Product');
  });

  it('updates a product', async () => {
    const { result } = renderHook(() => useUpdateProduct(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'product-1', body: { name: 'Updated Product', price: 9999 } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe('Updated Product');
  });

  it('deletes a product', async () => {
    const { result } = renderHook(() => useDeleteProduct(), { wrapper: createWrapper() });

    result.current.mutate('product-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it('returns an error when create fails', async () => {
    server.use(
      http.post('http://localhost:8080/products', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useCreateProduct(), { wrapper: createWrapper() });

    result.current.mutate({ name: 'Bad Product', price: 1, categoryId: 'category-1' });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Create failed');
  });
});
