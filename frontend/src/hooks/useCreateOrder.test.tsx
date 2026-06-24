import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCreateOrder } from './useCreateOrder';
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

describe('useCreateOrder', () => {
  it('creates an order and invalidates the orders query key', async () => {
    const { result } = renderHook(() => useCreateOrder(), { wrapper: createWrapper() });

    result.current.mutate({
      type: 'IN_PLACE',
      people: 2,
      tableId: 'table-1',
      details: [{ productId: 'product-1', quantity: 1 }],
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.id).toBe('order-new');
    expect(result.current.data?.type).toBe('IN_PLACE');
  });

  it('returns an error when create fails', async () => {
    server.use(
      http.post('http://localhost:8080/orders', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useCreateOrder(), { wrapper: createWrapper() });

    result.current.mutate({
      type: 'TAKE_AWAY',
      people: 1,
      details: [{ productId: 'product-1', quantity: 1 }],
    });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Create failed');
  });
});
