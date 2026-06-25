import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { ReactNode } from 'react';
import { LoginPage } from './LoginPage';

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
    return (
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      </MemoryRouter>
    );
  };
}

describe('LoginPage', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
  });

  it('renders the login form', () => {
    render(<LoginPage />, { wrapper: createWrapper() });

    expect(screen.getByRole('heading', { name: /sign in to tacosoft/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('displays an error message on failed login', async () => {
    render(<LoginPage />, { wrapper: createWrapper() });

    await userEvent.type(screen.getByLabelText(/username/i), 'admin');
    await userEvent.type(screen.getByLabelText(/password/i), 'wrong');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/invalid credentials/i);
  });
});
