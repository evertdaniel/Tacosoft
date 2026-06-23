import { describe, it, expect, afterEach, beforeAll, afterAll, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';

const { mockLogout, mockGetToken, mockGetCurrentRestaurantId, mockRedirectToLogin } = vi.hoisted(() => {
  return {
    mockLogout: vi.fn(),
    mockGetToken: vi.fn().mockReturnValue(null),
    mockGetCurrentRestaurantId: vi.fn().mockReturnValue(null),
    mockRedirectToLogin: vi.fn(),
  };
});

vi.mock('@/stores/auth.store', () => ({
  useAuthStore: {
    getState: () => ({
      token: mockGetToken(),
      logout: mockLogout,
    }),
  },
}));

vi.mock('@/stores/tenant.store', () => ({
  useTenantStore: {
    getState: () => ({
      currentRestaurantId: mockGetCurrentRestaurantId(),
    }),
  },
}));

vi.mock('@/utils/navigation', () => ({
  redirectToLogin: mockRedirectToLogin,
}));

import api from './axios';

const server = setupServer(
  http.get('http://localhost:8080/test-headers', ({ request }) => {
    return HttpResponse.json({
      authorization: request.headers.get('authorization'),
      restaurantId: request.headers.get('x-restaurant-id'),
    });
  }),
  http.get('http://localhost:8080/test-unauthorized', () => {
    return new HttpResponse(null, { status: 401 });
  }),
  http.get('http://localhost:8080/test-forbidden', () => {
    return new HttpResponse(null, { status: 403 });
  }),
  http.get('http://localhost:8080/test-error', () => {
    return new HttpResponse(null, { status: 500 });
  })
);

describe('axios client', () => {
  beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
  afterEach(() => {
    server.resetHandlers();
    vi.clearAllMocks();
    mockLogout.mockClear();
    mockRedirectToLogin.mockClear();
    mockGetToken.mockReturnValue(null);
    mockGetCurrentRestaurantId.mockReturnValue(null);
  });
  afterAll(() => server.close());

  it('uses default base URL for requests', async () => {
    mockGetToken.mockReturnValue('token-123');
    mockGetCurrentRestaurantId.mockReturnValue('rest-1');

    const { data } = await api.get('/test-headers');

    expect(data.authorization).toBe('Bearer token-123');
    expect(data.restaurantId).toBe('rest-1');
  });

  it('does not attach auth or tenant headers when token is missing', async () => {
    mockGetToken.mockReturnValue(null);
    mockGetCurrentRestaurantId.mockReturnValue('rest-1');

    const { data } = await api.get('/test-headers');

    expect(data.authorization).toBeNull();
    expect(data.restaurantId).toBeNull();
  });

  it('clears auth and redirects to login on 401', async () => {
    mockGetToken.mockReturnValue('token-123');

    await expect(api.get('/test-unauthorized')).rejects.toBeDefined();

    expect(mockLogout).toHaveBeenCalledOnce();
    expect(mockRedirectToLogin).toHaveBeenCalledOnce();
  });

  it('clears auth and redirects to login on 403', async () => {
    mockGetToken.mockReturnValue('token-123');

    await expect(api.get('/test-forbidden')).rejects.toBeDefined();

    expect(mockLogout).toHaveBeenCalledOnce();
    expect(mockRedirectToLogin).toHaveBeenCalledOnce();
  });

  it('does not redirect on other errors', async () => {
    mockGetToken.mockReturnValue('token-123');

    await expect(api.get('/test-error')).rejects.toBeDefined();

    expect(mockLogout).not.toHaveBeenCalled();
    expect(mockRedirectToLogin).not.toHaveBeenCalled();
  });
});
