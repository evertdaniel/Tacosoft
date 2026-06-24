import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useUpdateOrderDetailStatus } from './useUpdateOrderDetailStatus';
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

describe('useUpdateOrderDetailStatus', () => {
  it('updates the detail status and returns the updated detail', async () => {
    const { result } = renderHook(() => useUpdateOrderDetailStatus(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'detail-1', body: { status: 'READY' } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.id).toBe('detail-1');
    expect(result.current.data?.status).toBe('READY');
  });

  it('returns an error when the update fails', async () => {
    server.use(
      http.patch('http://localhost:8080/order-details/:id/status', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useUpdateOrderDetailStatus(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'detail-1', body: { status: 'DELIVERED' } });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Server error');
  });
});
