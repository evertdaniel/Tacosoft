import { describe, it, expect, beforeEach, vi } from 'vitest';
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
