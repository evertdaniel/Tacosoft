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

  it('derives currentRestaurantId but sets currentRole null when restaurantRoles has no match for the persisted restaurant', async () => {
    // currentRestaurant points to rest-3 which is NOT in restaurantRoles
    const roles = buildRestaurantRoles(); // only rest-1 and rest-2
    const unknownRestaurant: RestaurantInfoDto = { id: 'rest-3', name: 'Sucursal Sur' };

    mockStorage['currentRestaurant'] = JSON.stringify(unknownRestaurant);
    mockStorage['restaurantRoles'] = JSON.stringify(roles);

    const { useTenantStore: freshStore } = await import('./tenant.store');
    const { result } = renderHook(() => freshStore());

    expect(result.current.currentRestaurantId).toBe('rest-3');
    expect(result.current.currentRole).toBeNull();
    expect(result.current.availableRoles).toEqual(roles);
  });
});
