import { describe, expect, it, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { TenantSelector } from './TenantSelector';

function setupStore(roles: { restaurantId: string; restaurantName: string; roleName: 'ADMIN' | 'WAITER' | 'COOK' | 'CASHIER' }[], currentId: string) {
  const availableRoles = roles.map((r) => ({
    restaurantId: r.restaurantId,
    restaurantName: r.restaurantName,
    role: { id: `role-${r.roleName}`, name: r.roleName },
  }));

  const currentRole = availableRoles.find((r) => r.restaurantId === currentId)?.role ?? null;

  useTenantStore.setState({
    currentRestaurantId: currentId,
    currentRole,
    availableRoles,
  });
}

describe('TenantSelector', () => {
  beforeEach(() => {
    resetTenantStore();
  });

  it('is not visible when the user has a single restaurant role', () => {
    setupStore([{ restaurantId: 'rest-1', restaurantName: 'Taqueria Principal', roleName: 'ADMIN' }], 'rest-1');

    render(<TenantSelector />);
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
    expect(screen.getByText('Taqueria Principal')).toBeInTheDocument();
  });

  it('is visible when the user has multiple restaurant roles', () => {
    setupStore(
      [
        { restaurantId: 'rest-1', restaurantName: 'Taqueria Principal', roleName: 'ADMIN' },
        { restaurantId: 'rest-2', restaurantName: 'Taqueria Norte', roleName: 'WAITER' },
      ],
      'rest-1'
    );

    render(<TenantSelector />);
    expect(screen.getByRole('combobox')).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Taqueria Principal' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Taqueria Norte' })).toBeInTheDocument();
  });

  it('updates the tenant store when a different restaurant is selected', async () => {
    setupStore(
      [
        { restaurantId: 'rest-1', restaurantName: 'Taqueria Principal', roleName: 'ADMIN' },
        { restaurantId: 'rest-2', restaurantName: 'Taqueria Norte', roleName: 'WAITER' },
      ],
      'rest-1'
    );

    render(<TenantSelector />);
    await userEvent.selectOptions(screen.getByRole('combobox'), 'rest-2');

    expect(useTenantStore.getState().currentRestaurantId).toBe('rest-2');
    expect(useTenantStore.getState().currentRole?.name).toBe('WAITER');
  });
});
