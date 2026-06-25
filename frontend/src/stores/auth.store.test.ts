import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
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
import { useTenantStore, resetTenantStore } from './tenant.store';

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
      resetTenantStore();
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

  it('logout also removes restaurantRoles from storage', () => {
    // Pre-populate restaurantRoles in storage (written by tenant.store.setTenant or setAuth flow)
    const restaurantRoles = [
      {
        restaurantId: 'rest-1',
        restaurantName: 'Taqueria Principal',
        role: { id: 'role-1', name: role },
      },
    ];
    mockStorage['restaurantRoles'] = JSON.stringify(restaurantRoles);

    const login = buildLoginResponse('valid-token');
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setAuth(login);
    });

    act(() => {
      result.current.logout();
    });

    // restaurantRoles must be gone — not just token/user/currentRestaurant
    expect(mockStorage['restaurantRoles']).toBeUndefined();
  });

  it('logout resets in-memory tenant store so no stale x-restaurant-id can be sent before a reload', () => {
    // Arrange: set up a live tenant session in memory via the tenant store
    const roles = [
      {
        restaurantId: 'rest-1',
        restaurantName: 'Taqueria Principal',
        role: { id: 'role-1', name: role },
      },
    ];
    const currentRestaurant = { id: 'rest-1', name: 'Taqueria Principal', role: 'ADMIN' };

    act(() => {
      useTenantStore.getState().setTenant(
        roles.map((r) => ({
          restaurantId: r.restaurantId,
          restaurantName: r.restaurantName,
          role: r.role,
        })),
        currentRestaurant,
      );
    });

    // Confirm tenant store is populated before logout
    expect(useTenantStore.getState().currentRestaurantId).toBe('rest-1');
    expect(useTenantStore.getState().availableRoles).toHaveLength(1);

    const login = buildLoginResponse('valid-token');
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setAuth(login);
    });

    // Act: call logout — must reset tenant store in-memory WITHOUT requiring a page reload
    act(() => {
      result.current.logout();
    });

    // Assert: tenant in-memory state is cleared immediately (no reload needed)
    expect(useTenantStore.getState().currentRestaurantId).toBeNull();
    expect(useTenantStore.getState().currentRole).toBeNull();
    expect(useTenantStore.getState().availableRoles).toHaveLength(0);
  });
});

describe('auth store — rehydration after logout', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    vi.resetModules();
  });

  afterEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    vi.resetModules();
  });

  it('a reload after logout yields empty availableRoles in tenant store', async () => {
    // Setup: simulate a live session already stored
    const restaurantRoles = [
      {
        restaurantId: 'rest-1',
        restaurantName: 'Taqueria Principal',
        role: { id: 'role-1', name: role },
      },
    ];
    const currentRestaurant = { id: 'rest-1', name: 'Taqueria Principal', role: 'ADMIN' };
    const token = 'valid-token';
    const user = {
      id: 'user-1',
      username: 'admin',
      firstName: 'Admin',
      lastName: 'User',
      email: 'admin@tacosoft.com',
      active: true,
      primaryRole: { id: 'role-1', name: role },
      restaurantRoles,
    };

    mockStorage['token'] = JSON.stringify(token);
    mockStorage['user'] = JSON.stringify(user);
    mockStorage['currentRestaurant'] = JSON.stringify(currentRestaurant);
    mockStorage['restaurantRoles'] = JSON.stringify(restaurantRoles);

    // Boot auth store and call logout — this must also clear restaurantRoles
    const { useAuthStore: freshAuth } = await import('./auth.store');
    const { result: authResult } = renderHook(() => freshAuth());

    act(() => {
      authResult.current.logout();
    });

    expect(authResult.current.token).toBeNull();
    expect(authResult.current.isAuthenticated).toBe(false);
    // restaurantRoles key must be absent from storage after logout
    expect(mockStorage['restaurantRoles']).toBeUndefined();
    expect(mockStorage['currentRestaurant']).toBeUndefined();

    // Simulate reload: re-import tenant store — availableRoles must be empty
    vi.resetModules();
    const { useTenantStore: freshTenant } = await import('../stores/tenant.store');
    const { result: tenantResult } = renderHook(() => freshTenant());

    expect(tenantResult.current.availableRoles).toEqual([]);
    expect(tenantResult.current.currentRestaurantId).toBeNull();
    expect(tenantResult.current.currentRole).toBeNull();
  });
});
