import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { DashboardPage } from './DashboardPage';
import { dashboardReportFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';
import userEvent from '@testing-library/user-event';

const { mockStorage } = vi.hoisted(() => ({
  mockStorage: {} as Record<string, string | null>,
}));

vi.mock('@/utils/storage', () => ({
  getItem: vi.fn(function getItem<T>(key: string): T | null {
    const raw = mockStorage[key];
    if (raw === undefined || raw === null) return null;
    try {
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }),
  setItem: vi.fn(function setItem<T>(key: string, value: T) {
    mockStorage[key] = JSON.stringify(value);
  }),
  removeItem: vi.fn((key: string) => {
    delete mockStorage[key];
  }),
}));

vi.mock('@/utils/jwt', () => ({
  decodeExp: vi.fn(() => 9999999999),
}));

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe('DashboardPage', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
  });

  it('shows a loading state while fetching', () => {
    render(<DashboardPage />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading dashboard/i)).toBeInTheDocument();
  });

  it('renders the KPI grid on success', async () => {
    render(<DashboardPage />, { wrapper: createWrapper() });

    expect(await screen.findByText(dashboardReportFixture.activeOrders.toString())).toBeInTheDocument();
    expect(screen.getByText('Active Orders')).toBeInTheDocument();
    expect(screen.getByText('Sales Today')).toBeInTheDocument();
  });

  it('shows an error state with retry on failure', async () => {
    server.use(
      http.get('http://localhost:8080/reports/dashboard', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), {
          status: 500,
          headers: { 'Content-Type': 'application/json' },
        });
      })
    );

    render(<DashboardPage />, { wrapper: createWrapper() });

    expect(await screen.findByRole('alert')).toBeInTheDocument();
    expect(screen.getByText(/dashboard unavailable/i)).toBeInTheDocument();
    expect(screen.getByText(/server error/i)).toBeInTheDocument();

    const retryButton = screen.getByRole('button', { name: /retry/i });
    expect(retryButton).toBeInTheDocument();

    server.resetHandlers();
    await userEvent.click(retryButton);

    expect(await screen.findByText(dashboardReportFixture.activeOrders.toString())).toBeInTheDocument();
  });
});
