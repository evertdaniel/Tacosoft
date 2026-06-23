import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { LoginResponse, Role } from '@/types/domain.types';

const { mockStorage } = vi.hoisted(() => ({
  mockStorage: {} as Record<string, string | null>,
}));

vi.mock('@/utils/storage', () => ({
  getItem: vi.fn(<T>(key: string): T | null => {
    const raw = mockStorage[key];
    if (raw === undefined || raw === null) return null;
    try {
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }),
  setItem: vi.fn(<T>(key: string, value: T) => {
    mockStorage[key] = JSON.stringify(value);
  }),
  removeItem: vi.fn((key: string) => {
    delete mockStorage[key];
  }),
}));

vi.mock('@/utils/jwt', () => ({
  decodeExp: vi.fn((token: string) => {
    if (token === 'expired-token') return Math.floor(Date.now() / 1000) - 1;
    if (token === 'valid-token') return Math.floor(Date.now() / 1000) + 3600;
    return null;
  }),
}));

import { useAuthStore, resetAuthStore } from './auth.store';

const role: Role = 'ADMIN';

function buildLoginResponse(token: string): LoginResponse {
  return {
    token,
    user: {
      id: 'user-1',
      username: 'admin',
      firstName: 'Admin',
      lastName: 'User',
      email: 'admin@tacosoft.com',
      active: true,
      primaryRole: { id: 'role-1', name: role },
      restaurantRoles: [
        {
          restaurantId: 'rest-1',
          restaurantName: 'Taqueria Principal',
          role: { id: 'role-1', name: role },
        },
      ],
    },
    currentRestaurant: { id: 'rest-1', name: 'Taqueria Principal', role: 'ADMIN' },
  };
}

describe('auth store', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    act(() => {
      resetAuthStore();
    });
  });

  it('initializes unauthenticated with no token', () => {
    const { result } = renderHook(() => useAuthStore());

    expect(result.current.token).toBeNull();
    expect(result.current.user).toBeNull();
    expect(result.current.currentRestaurant).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it('setAuth persists token, user and currentRestaurant to storage and state', () => {
    const login = buildLoginResponse('valid-token');
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setAuth(login);
    });

    expect(result.current.token).toBe(login.token);
    expect(result.current.user).toEqual(login.user);
    expect(result.current.currentRestaurant).toEqual(login.currentRestaurant);
    expect(result.current.isAuthenticated).toBe(true);

    expect(mockStorage['token']).toBe(JSON.stringify(login.token));
    expect(mockStorage['user']).toBe(JSON.stringify(login.user));
    expect(mockStorage['currentRestaurant']).toBe(JSON.stringify(login.currentRestaurant));
  });

  it('logout clears state and storage auth keys', () => {
    const login = buildLoginResponse('valid-token');
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setAuth(login);
    });

    act(() => {
      result.current.logout();
    });

    expect(result.current.token).toBeNull();
    expect(result.current.user).toBeNull();
    expect(result.current.currentRestaurant).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
    expect(mockStorage['token']).toBeUndefined();
    expect(mockStorage['user']).toBeUndefined();
    expect(mockStorage['currentRestaurant']).toBeUndefined();
  });

  it('isTokenExpired returns false for a token that has not expired', () => {
    const login = buildLoginResponse('valid-token');
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setAuth(login);
    });

    expect(result.current.isTokenExpired()).toBe(false);
  });

  it('isTokenExpired returns true for an expired token', () => {
    const login = buildLoginResponse('expired-token');
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setAuth(login);
    });

    expect(result.current.isTokenExpired()).toBe(true);
  });
});
