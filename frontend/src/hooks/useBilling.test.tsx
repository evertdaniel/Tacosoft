import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useInvoices, useUnpaidInvoices, usePayInvoice } from './useBilling';
import { invoicesFixture, unpaidInvoicesFixture, paymentBodyFixture } from '@/test/fixtures';
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

describe('useInvoices', () => {
  it('returns loading then the list of invoices', async () => {
    const { result } = renderHook(() => useInvoices(), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(invoicesFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/invoices', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useInvoices(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Server error');
  });
});

describe('useUnpaidInvoices', () => {
  it('returns only unpaid invoices', async () => {
    const { result } = renderHook(() => useUnpaidInvoices(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(unpaidInvoicesFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/invoices/unpaid', () => {
        return new HttpResponse(JSON.stringify({ message: 'Unpaid error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useUnpaidInvoices(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Unpaid error');
  });
});

describe('usePayInvoice', () => {
  it('pays an invoice and invalidates invoice query keys', async () => {
    const { result } = renderHook(() => usePayInvoice(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'invoice-1', body: paymentBodyFixture });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.id).toBe('invoice-1');
    expect(result.current.data?.isPaid).toBe(true);
  });

  it('returns an error when payment fails', async () => {
    server.use(
      http.post('http://localhost:8080/invoices/:id/pay', () => {
        return new HttpResponse(JSON.stringify({ message: 'Pay failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => usePayInvoice(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'invoice-1', body: paymentBodyFixture });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Pay failed');
  });
});
