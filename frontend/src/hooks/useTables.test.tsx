import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useTables } from './useTables';
import { tablesFixture } from '@/test/fixtures';
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

describe('useTables', () => {
  it('returns loading then the list of tables', async () => {
    const { result } = renderHook(() => useTables(), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(tablesFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/tables', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useTables(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Server error');
  });
});
