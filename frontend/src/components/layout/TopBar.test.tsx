import { describe, expect, it, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { TopBar } from './TopBar';

const { mockStorage } = vi.hoisted(() => ({
  mockStorage: {} as Record<string, string | null>,
}));

vi.mock('@/utils/storage', () => ({
  getItem: vi.fn((key: string) => {
    const raw = mockStorage[key];
    if (raw === undefined || raw === null) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }) as <T>(key: string) => T | null,
  setItem: vi.fn((key: string, value: unknown) => {
    mockStorage[key] = JSON.stringify(value);
  }) as <T>(key: string, value: T) => void,
  removeItem: vi.fn((key: string) => {
    delete mockStorage[key];
  }),
}));

describe('TopBar', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    resetAuthStore();
    resetTenantStore();
  });

  it('displays the current restaurant name and role', () => {
    useAuthStore.setState({
      token: 'token',
      user: null,
      currentRestaurant: { id: 'rest-1', name: 'Taqueria Principal' },
      expiresAt: null,
      isAuthenticated: true,
    });
    useTenantStore.setState({
      currentRestaurantId: 'rest-1',
      currentRole: { id: 'role-admin', name: 'ADMIN' },
      availableRoles: [],
    });

    render(
      <MemoryRouter>
        <TopBar />
      </MemoryRouter>
    );

    expect(screen.getByText('Taqueria Principal')).toBeInTheDocument();
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
  });

  it('logs out when the logout button is clicked', async () => {
    useAuthStore.setState({
      token: 'token',
      user: null,
      currentRestaurant: { id: 'rest-1', name: 'Taqueria Principal' },
      expiresAt: null,
      isAuthenticated: true,
    });
    useTenantStore.setState({
      currentRestaurantId: 'rest-1',
      currentRole: { id: 'role-admin', name: 'ADMIN' },
      availableRoles: [],
    });

    render(
      <MemoryRouter>
        <TopBar />
      </MemoryRouter>
    );

    await userEvent.click(screen.getByRole('button', { name: 'Logout' }));
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().token).toBeNull();
  });
});
