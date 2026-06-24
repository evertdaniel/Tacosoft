import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { readFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { http, HttpResponse } from 'msw';
import { Shell } from '@/components/layout/Shell';
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { server } from './server';
import { loginResponseFixture } from './handlers';

const __dirname = dirname(fileURLToPath(import.meta.url));

describe('PWA manifest', () => {
  it('has the required fields in public/manifest.json', () => {
    const manifest = JSON.parse(
      readFileSync(resolve(__dirname, '../../public/manifest.json'), 'utf8')
    );

    expect(manifest.name).toBe('Tacosoft');
    expect(manifest.short_name).toBe('Tacosoft');
    expect(manifest.start_url).toBe('/');
    expect(manifest.display).toMatch(/standalone|fullscreen|minimal-ui/);
    expect(manifest.icons).toBeInstanceOf(Array);
    expect(manifest.icons.length).toBeGreaterThan(0);

    manifest.icons.forEach(
      (icon: { src?: string; sizes?: string; type?: string }) => {
        expect(icon.src).toBeDefined();
        expect(icon.sizes).toBeDefined();
        expect(icon.type).toBeDefined();
      }
    );
  });

  it('configures vite-plugin-pwa with a manifest', () => {
    const configSource = readFileSync(
      resolve(__dirname, '../../vite.config.ts'),
      'utf8'
    );

    expect(configSource).toContain("from 'vite-plugin-pwa'");
    expect(configSource).toContain('VitePWA(');
    expect(configSource).toContain('manifest:');
  });
});

describe('Offline shell', () => {
  beforeEach(() => {
    Object.defineProperty(window.navigator, 'onLine', {
      configurable: true,
      value: false,
    });

    server.use(
      http.get('http://localhost:8080/reports/dashboard', () => {
        return new HttpResponse(JSON.stringify({ message: 'offline' }), {
          status: 503,
          headers: { 'Content-Type': 'application/json' },
        });
      })
    );

    useAuthStore.setState({
      token: loginResponseFixture.token,
      user: loginResponseFixture.user,
      currentRestaurant: loginResponseFixture.currentRestaurant,
      expiresAt: 9999999999,
      isAuthenticated: true,
    });

    useTenantStore.setState({
      currentRestaurantId: loginResponseFixture.currentRestaurant.id,
      currentRole: loginResponseFixture.user.restaurantRoles[0].role,
      availableRoles: loginResponseFixture.user.restaurantRoles,
    });
  });

  afterEach(() => {
    Object.defineProperty(window.navigator, 'onLine', {
      configurable: true,
      value: true,
    });
    server.resetHandlers();
    resetAuthStore();
    resetTenantStore();
  });

  it('renders the app shell when the dashboard request fails while offline', async () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false, refetchOnWindowFocus: false },
        mutations: { retry: false },
      },
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/dashboard']}>
          <Routes>
            <Route path="/" element={<Shell />}>
              <Route path="dashboard" element={<DashboardPage />} />
            </Route>
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );

    expect(screen.getByRole('banner')).toHaveTextContent('Taqueria Principal');
    expect(screen.getByRole('banner')).toHaveTextContent('ADMIN');
    expect(screen.getByLabelText('Sidebar navigation')).toBeInTheDocument();
    expect(screen.getAllByRole('link', { name: /dashboard/i })).toHaveLength(2);
    expect(
      await screen.findByRole('heading', { name: /dashboard unavailable/i })
    ).toBeInTheDocument();
  });
});
