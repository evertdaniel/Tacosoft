import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useOrder } from './useOrder';
import { ordersFixture } from '@/test/fixtures';
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

describe('useOrder', () => {
  it('returns loading then the order', async () => {
    const { result } = renderHook(() => useOrder('order-2'), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(ordersFixture[1]);
  });

  it('does not fetch when id is empty', async () => {
    const { result } = renderHook(() => useOrder(''), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.fetchStatus).toBe('idle');
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/orders/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Not found' }), { status: 404 });
      })
    );

    const { result } = renderHook(() => useOrder('order-1'), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Not found');
  });
});
