import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useLogin } from './useLogin';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { loginResponseFixture } from '@/test/handlers';

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

describe('useLogin', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    resetAuthStore();
    resetTenantStore();
  });

  it('persists auth and tenant state on successful login', async () => {
    const { result } = renderHook(() => useLogin(), { wrapper: createWrapper() });

    result.current.mutate({ username: 'admin', password: 'secret' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(useAuthStore.getState().token).toBe(loginResponseFixture.token);
    expect(useAuthStore.getState().user).toEqual(loginResponseFixture.user);
    expect(useAuthStore.getState().currentRestaurant).toEqual(loginResponseFixture.currentRestaurant);

    expect(useTenantStore.getState().currentRestaurantId).toBe(loginResponseFixture.currentRestaurant.id);
    expect(useTenantStore.getState().availableRoles).toEqual(loginResponseFixture.user.restaurantRoles);
  });

  it('exposes error on failed login without changing state', async () => {
    const { result } = renderHook(() => useLogin(), { wrapper: createWrapper() });

    result.current.mutate({ username: 'admin', password: 'wrong' });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useTenantStore.getState().currentRestaurantId).toBeNull();
  });
});
