import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useTokenExpiry } from './useTokenExpiry';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';

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

const originalLocation = window.location;

describe('useTokenExpiry', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    resetAuthStore();
    vi.useFakeTimers();
    Object.defineProperty(window, 'location', {
      writable: true,
      value: { href: '/dashboard' },
    });
  });

  afterEach(() => {
    vi.useRealTimers();
    Object.defineProperty(window, 'location', {
      writable: true,
      value: originalLocation,
    });
  });

  it('logs out and redirects when expiry is in the past', () => {
    useAuthStore.setState({
      token: 'valid-token',
      user: null,
      currentRestaurant: null,
      expiresAt: 1,
      isAuthenticated: true,
    });

    renderHook(() => useTokenExpiry());

    act(() => {
      vi.advanceTimersByTime(60_000);
    });

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().token).toBeNull();
    expect(window.location.href).toBe('/login');
  });

  it('does nothing when token is not expired', () => {
    const futureExp = Math.floor(Date.now() / 1000) + 3600;

    useAuthStore.setState({
      token: 'valid-token',
      user: null,
      currentRestaurant: null,
      expiresAt: futureExp,
      isAuthenticated: true,
    });

    renderHook(() => useTokenExpiry());

    act(() => {
      vi.advanceTimersByTime(60_000);
    });

    expect(useAuthStore.getState().isAuthenticated).toBe(true);
    expect(useAuthStore.getState().token).toBe('valid-token');
    expect(window.location.href).toBe('/dashboard');
  });

  it('clears interval on unmount', () => {
    useAuthStore.setState({
      token: 'valid-token',
      user: null,
      currentRestaurant: null,
      expiresAt: 1,
      isAuthenticated: true,
    });

    const { unmount } = renderHook(() => useTokenExpiry());
    unmount();

    act(() => {
      vi.advanceTimersByTime(120_000);
    });

    expect(useAuthStore.getState().isAuthenticated).toBe(true);
    expect(window.location.href).toBe('/dashboard');
  });
});
