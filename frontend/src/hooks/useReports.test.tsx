import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useSalesReport, useProductReport, useFinancialReport, useFootfallReport, useStaffPlanningReport } from './useReports';
import {
  salesSummaryFixture,
  productReportsFixture,
  financialReportFixture,
  footfallReportFixture,
  staffPlanningReportFixture,
  reportDateRangeFixture,
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

describe('useSalesReport', () => {
  it('returns the sales summary for the date range', async () => {
    const { result } = renderHook(() => useSalesReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(salesSummaryFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/sales', () => {
        return new HttpResponse(JSON.stringify({ message: 'Sales error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useSalesReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Sales error');
  });
});

describe('useProductReport', () => {
  it('returns the product report list for the date range', async () => {
    const { result } = renderHook(() => useProductReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(productReportsFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/products', () => {
        return new HttpResponse(JSON.stringify({ message: 'Products error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useProductReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Products error');
  });
});

describe('useFinancialReport', () => {
  it('returns the financial report for the date range', async () => {
    const { result } = renderHook(() => useFinancialReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(financialReportFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/finances', () => {
        return new HttpResponse(JSON.stringify({ message: 'Finances error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useFinancialReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Finances error');
  });
});

describe('useFootfallReport', () => {
  it('returns the footfall report for the end date of the range', async () => {
    const { result } = renderHook(() => useFootfallReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(footfallReportFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/footfall', () => {
        return new HttpResponse(JSON.stringify({ message: 'Footfall error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useFootfallReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Footfall error');
  });
});

describe('useStaffPlanningReport', () => {
  it('returns the staff planning report for the end date of the range', async () => {
    const { result } = renderHook(() => useStaffPlanningReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(staffPlanningReportFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/staff-planning', () => {
        return new HttpResponse(JSON.stringify({ message: 'Staff planning error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useStaffPlanningReport(reportDateRangeFixture), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Staff planning error');
  });
});
