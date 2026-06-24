import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  useCashRegisters,
  useActiveCashRegister,
  useOpenCashRegister,
  useCloseCashRegister,
  useXReport,
  useZReport,
} from './useCash';
import {
  cashRegistersFixture,
  activeCashRegisterFixture,
  openCashRegisterBodyFixture,
  closeCashRegisterBodyFixture,
  xReportFixture,
  zReportFixture,
} from '@/test/fixtures';
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

describe('useCashRegisters', () => {
  it('returns the list of cash registers', async () => {
    const { result } = renderHook(() => useCashRegisters(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(cashRegistersFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/cash-registers', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useCashRegisters(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Server error');
  });
});

describe('useActiveCashRegister', () => {
  it('returns the active cash register', async () => {
    const { result } = renderHook(() => useActiveCashRegister(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(activeCashRegisterFixture);
  });

  it('returns an error when there is no active register', async () => {
    server.use(
      http.get('http://localhost:8080/cash-registers/active', () => {
        return new HttpResponse(JSON.stringify({ message: 'No active register' }), { status: 404 });
      })
    );

    const { result } = renderHook(() => useActiveCashRegister(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('No active register');
  });
});

describe('useOpenCashRegister', () => {
  it('opens a register and invalidates cash queries', async () => {
    const { result } = renderHook(() => useOpenCashRegister(), { wrapper: createWrapper() });

    result.current.mutate(openCashRegisterBodyFixture);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.status).toBe('OPEN');
    expect(result.current.data?.openingAmount).toBe(openCashRegisterBodyFixture.openingAmount);
  });

  it('returns an error when opening fails', async () => {
    server.use(
      http.post('http://localhost:8080/cash-registers/open', () => {
        return new HttpResponse(JSON.stringify({ message: 'Open failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useOpenCashRegister(), { wrapper: createWrapper() });

    result.current.mutate(openCashRegisterBodyFixture);

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Open failed');
  });
});

describe('useCloseCashRegister', () => {
  it('closes a register and returns the Z report', async () => {
    const { result } = renderHook(() => useCloseCashRegister(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'cash-1', body: closeCashRegisterBodyFixture });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.cashRegisterId).toBe('cash-1');
    expect(result.current.data?.declaredAmount).toBe(closeCashRegisterBodyFixture.closingAmount);
  });

  it('returns an error when closing fails', async () => {
    server.use(
      http.put('http://localhost:8080/cash-registers/:id/close', () => {
        return new HttpResponse(JSON.stringify({ message: 'Close failed' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useCloseCashRegister(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'cash-1', body: closeCashRegisterBodyFixture });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Close failed');
  });
});

describe('useXReport', () => {
  it('returns the X report', async () => {
    const { result } = renderHook(() => useXReport(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(xReportFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/cash-registers/x-report', () => {
        return new HttpResponse(JSON.stringify({ message: 'X report error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useXReport(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('X report error');
  });
});

describe('useZReport', () => {
  it('returns the Z report', async () => {
    const { result } = renderHook(() => useZReport(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(zReportFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/cash-registers/z-report', () => {
        return new HttpResponse(JSON.stringify({ message: 'Z report error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useZReport(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Z report error');
  });
});
