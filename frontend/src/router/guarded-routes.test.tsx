import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from './guarded-routes';
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

vi.mock('@/utils/jwt', () => ({
  decodeExp: vi.fn(() => 9999999999),
}));

function renderWithRouter(initialEntries: string[]) {
  return render(
    <MemoryRouter
      initialEntries={initialEntries}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/login" element={<div data-testid="login-page">Login</div>} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <div data-testid="dashboard-page">Dashboard</div>
            </ProtectedRoute>
          }
        />
      </Routes>
    </MemoryRouter>
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    Object.keys(mockStorage).forEach((key) => delete mockStorage[key]);
    resetAuthStore();
  });

  it('redirects unauthenticated users to /login', () => {
    renderWithRouter(['/dashboard']);

    expect(screen.getByTestId('login-page')).toBeInTheDocument();
    expect(screen.queryByTestId('dashboard-page')).not.toBeInTheDocument();
  });

  it('renders children when the user is authenticated', () => {
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

    renderWithRouter(['/dashboard']);

    expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
    expect(screen.queryByTestId('login-page')).not.toBeInTheDocument();
  });
});
