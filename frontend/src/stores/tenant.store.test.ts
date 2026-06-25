import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { RestaurantInfoDto, RestaurantRoleDto, Role, RoleDto } from '@/types/domain.types';

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

import { useTenantStore, resetTenantStore } from './tenant.store';

function buildRole(name: Role): RoleDto {
  return { id: `role-${name}`, name };
}

function buildRestaurantRoles(): RestaurantRoleDto[] {
  return [
    { restaurantId: 'rest-1', restaurantName: 'Taqueria Principal', role: buildRole('ADMIN') },
    { restaurantId: 'rest-2', restaurantName: 'Sucursal Norte', role: buildRole('WAITER') },
  ];
}

function buildCurrentRestaurant(): RestaurantInfoDto {
  return { id: 'rest-1', name: 'Taqueria Principal', role: 'ADMIN' };
}

describe('tenant store', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    act(() => {
      resetTenantStore();
    });
  });

  it('initializes with empty tenant state', () => {
    const { result } = renderHook(() => useTenantStore());

    expect(result.current.currentRestaurantId).toBeNull();
    expect(result.current.currentRole).toBeNull();
    expect(result.current.availableRoles).toEqual([]);
  });

  it('setTenant initializes current restaurant, role and available roles', () => {
    const roles = buildRestaurantRoles();
    const current = buildCurrentRestaurant();
    const { result } = renderHook(() => useTenantStore());

    act(() => {
      result.current.setTenant(roles, current);
    });

    expect(result.current.currentRestaurantId).toBe('rest-1');
    expect(result.current.currentRole).toEqual(buildRole('ADMIN'));
    expect(result.current.availableRoles).toEqual(roles);
  });

  it('setTenant persists restaurantRoles to storage', () => {
    const roles = buildRestaurantRoles();
    const current = buildCurrentRestaurant();
    const { result } = renderHook(() => useTenantStore());

    act(() => {
      result.current.setTenant(roles, current);
    });

    expect(mockStorage['restaurantRoles']).toBe(JSON.stringify(roles));
  });

  it('switchRestaurant updates current restaurant and role', () => {
    const roles = buildRestaurantRoles();
    const current = buildCurrentRestaurant();
    const { result } = renderHook(() => useTenantStore());

    act(() => {
      result.current.setTenant(roles, current);
    });

    act(() => {
      result.current.switchRestaurant('rest-2');
    });

    expect(result.current.currentRestaurantId).toBe('rest-2');
    expect(result.current.currentRole).toEqual(buildRole('WAITER'));
  });

  it('switchRestaurant ignores unknown restaurant ids', () => {
    const roles = buildRestaurantRoles();
    const current = buildCurrentRestaurant();
    const { result } = renderHook(() => useTenantStore());

    act(() => {
      result.current.setTenant(roles, current);
    });

    act(() => {
      result.current.switchRestaurant('unknown');
    });

    expect(result.current.currentRestaurantId).toBe('rest-1');
    expect(result.current.currentRole).toEqual(buildRole('ADMIN'));
  });
});

describe('tenant store — rehydration from persisted session', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    vi.resetModules();
  });

  afterEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    vi.resetModules();
  });

  it('derives currentRestaurantId and currentRole from persisted storage on boot', async () => {
    // Simulate what auth.store writes after login (persisted session)
    const roles = buildRestaurantRoles();
    const currentRestaurant = buildCurrentRestaurant();

    mockStorage['currentRestaurant'] = JSON.stringify(currentRestaurant);
    mockStorage['restaurantRoles'] = JSON.stringify(roles);

    // Re-import after vi.resetModules() so loadInitialState() runs with the pre-populated storage
    const { useTenantStore: freshStore } = await import('./tenant.store');
    const { result } = renderHook(() => freshStore());

    expect(result.current.currentRestaurantId).toBe('rest-1');
    expect(result.current.currentRole).toEqual(buildRole('ADMIN'));
    expect(result.current.availableRoles).toEqual(roles);
  });

  it('leaves currentRestaurantId and currentRole null when no session is persisted', async () => {
    // No data in mockStorage — cold boot with no prior login
    const { useTenantStore: freshStore } = await import('./tenant.store');
    const { result } = renderHook(() => freshStore());

    expect(result.current.currentRestaurantId).toBeNull();
    expect(result.current.currentRole).toBeNull();
    expect(result.current.availableRoles).toEqual([]);
  });

  it('sets both currentRestaurantId and currentRole to null when restaurantRoles has no match for the persisted restaurant (security guard)', async () => {
    // currentRestaurant points to rest-3 which is NOT in restaurantRoles —
    // the store must reject the unmatched restaurant entirely so the interceptor
    // never sends x-restaurant-id for a tenant the user has no role for.
    const roles = buildRestaurantRoles(); // only rest-1 and rest-2
    const unknownRestaurant: RestaurantInfoDto = { id: 'rest-3', name: 'Sucursal Sur' };

    mockStorage['currentRestaurant'] = JSON.stringify(unknownRestaurant);
    mockStorage['restaurantRoles'] = JSON.stringify(roles);

    const { useTenantStore: freshStore } = await import('./tenant.store');
    const { result } = renderHook(() => freshStore());

    // Both must be null — an unmatched restaurant id must never reach the interceptor.
    expect(result.current.currentRestaurantId).toBeNull();
    expect(result.current.currentRole).toBeNull();
    // availableRoles is still populated so the UI can offer a valid restaurant to switch to.
    expect(result.current.availableRoles).toEqual(roles);
  });

  it('switchRestaurant persists switched restaurant so a reload rehydrates the new selection', async () => {
    // Setup: pre-populate storage as if auth.store.setAuth already ran for rest-1
    const roles = buildRestaurantRoles();
    const currentRestaurant = buildCurrentRestaurant(); // rest-1

    mockStorage['currentRestaurant'] = JSON.stringify(currentRestaurant);
    mockStorage['restaurantRoles'] = JSON.stringify(roles);

    // Boot the store so it rehydrates from storage (loadInitialState picks up rest-1)
    const { useTenantStore: freshStore } = await import('./tenant.store');
    const { result } = renderHook(() => freshStore());

    expect(result.current.currentRestaurantId).toBe('rest-1');

    // Switch to rest-2 — this should PERSIST currentRestaurant to storage
    act(() => {
      result.current.switchRestaurant('rest-2');
    });

    // Verify in-memory state updated
    expect(result.current.currentRestaurantId).toBe('rest-2');
    expect(result.current.currentRole).toEqual(buildRole('WAITER'));

    // Simulate a page reload: reset modules so loadInitialState runs fresh from storage
    vi.resetModules();
    const { useTenantStore: reloadedStore } = await import('./tenant.store');
    const { result: reloaded } = renderHook(() => reloadedStore());

    // After reload, the SWITCHED restaurant (rest-2) must be rehydrated — not the original (rest-1)
    expect(reloaded.current.currentRestaurantId).toBe('rest-2');
    expect(reloaded.current.currentRole).toEqual(buildRole('WAITER'));
  });

  it('switchRestaurant only persists restaurants that exist in the users roles (security guard)', async () => {
    // Ensure switchRestaurant never persists a restaurant not in availableRoles
    const roles = buildRestaurantRoles();
    const currentRestaurant = buildCurrentRestaurant(); // rest-1

    mockStorage['currentRestaurant'] = JSON.stringify(currentRestaurant);
    mockStorage['restaurantRoles'] = JSON.stringify(roles);

    const { useTenantStore: freshStore } = await import('./tenant.store');
    const { result } = renderHook(() => freshStore());

    // Attempt switch to a restaurant NOT in the user's roles — must be a no-op
    act(() => {
      result.current.switchRestaurant('rest-999');
    });

    // In-memory state must be unchanged
    expect(result.current.currentRestaurantId).toBe('rest-1');

    // Storage must still contain rest-1, NOT rest-999
    const persisted = JSON.parse(mockStorage['currentRestaurant']!) as RestaurantInfoDto;
    expect(persisted.id).toBe('rest-1');
  });
});
