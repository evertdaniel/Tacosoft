import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { createMemoryRouter } from 'react-router-dom';
import App from './App';
import { routes } from './router';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';

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

function createTestRouter(initialEntry: string) {
  return createMemoryRouter(routes, {
    initialEntries: [initialEntry],
    future: {
      v7_startTransition: true,
      v7_relativeSplatPath: true,
    },
  });
}

describe('App routing', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    resetAuthStore();
    resetTenantStore();
  });

  it('renders the login page at /login', async () => {
    const router = createTestRouter('/login');
    render(<App router={router} />);

    expect(await screen.findByRole('heading', { name: /sign in to tacosoft/i })).toBeInTheDocument();
  });

  it('renders the protected shell for authenticated users', async () => {
    useAuthStore.setState({
      token: 'valid-token',
      user: {
        id: 'user-1',
        username: 'admin',
        firstName: 'Admin',
        lastName: 'User',
        email: 'admin@tacosoft.com',
        active: true,
        primaryRole: { id: 'role-1', name: 'ADMIN' },
        restaurantRoles: [],
      },
      currentRestaurant: { id: 'rest-1', name: 'Taqueria Principal' },
      isAuthenticated: true,
    });
    useTenantStore.setState({
      currentRestaurantId: 'rest-1',
      currentRole: { id: 'role-1', name: 'ADMIN' },
      availableRoles: [],
    });

    const router = createTestRouter('/dashboard');
    render(<App router={router} />);

    expect(await screen.findByRole('banner')).toBeInTheDocument();
    expect(await screen.findByTestId('dashboard-placeholder')).toBeInTheDocument();
  });
});
