import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useUpdateTableStatus } from './useUpdateTableStatus';
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

describe('useUpdateTableStatus', () => {
  it('mutates the table status and returns the updated table', async () => {
    const { result } = renderHook(() => useUpdateTableStatus(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'table-2', status: 'AVAILABLE' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.status).toBe('AVAILABLE');
    expect(result.current.data?.id).toBe('table-2');
  });

  it('returns an error when the status update fails', async () => {
    server.use(
      http.put('http://localhost:8080/tables/:id/status', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useUpdateTableStatus(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'table-1', status: 'CLEANING' });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Server error');
  });
});
