import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { Client } from '@stomp/stompjs';
import { useTables } from './useTables';
import { useWebSocket } from './useWebSocket';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';
import { TableDto } from '@/types/domain.types';

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn(),
}));

vi.mock('sockjs-client', () => ({
  default: vi.fn(() => ({})),
}));

let subscribeCallback: ((message: { body: string }) => void) | null = null;
let capturedOptions: Record<string, unknown> | null = null;

type MockClient = {
  activate: ReturnType<typeof vi.fn>;
  deactivate: ReturnType<typeof vi.fn>;
  subscribe: ReturnType<typeof vi.fn>;
};

function createMockClient(options: Record<string, unknown>): MockClient {
  capturedOptions = options;
  subscribeCallback = null;
  return {
    activate: vi.fn(() => {
      if (typeof options.onConnect === 'function') {
        (options.onConnect as () => void)();
      }
    }),
    deactivate: vi.fn(),
    subscribe: vi.fn((_destination, callback) => {
      subscribeCallback = callback;
      return { unsubscribe: vi.fn() };
    }),
  };
}

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  return function Wrapper({ children }: { children: React.ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

function StatusLabel() {
  const { data } = useTables();
  return <span data-testid="first-status">{data?.[0]?.status ?? 'loading'}</span>;
}

function Root() {
  useWebSocket();
  return <StatusLabel />;
}

describe('useWebSocket', () => {
  beforeEach(() => {
    resetAuthStore();
    resetTenantStore();
    (Client as unknown as ReturnType<typeof vi.fn>).mockReset();
    (Client as unknown as ReturnType<typeof vi.fn>).mockImplementation(createMockClient);
  });

  it('does not connect when there is no auth token', () => {
    render(
      <Root />,
      { wrapper: createWrapper() }
    );

    expect(Client).not.toHaveBeenCalled();
  });

  it('connects, subscribes to the tables topic, and invalidates tables on message', async () => {
    useAuthStore.setState({
      token: 'valid-token',
      user: null,
      currentRestaurant: null,
      expiresAt: null,
      isAuthenticated: true,
    });
    useTenantStore.setState({
      currentRestaurantId: 'rest-1',
      currentRole: null,
      availableRoles: [],
    });

    const updatedTables: TableDto[] = [
      { id: 'table-1', num: 1, seats: 4, status: 'OCCUPIED', posX: 0, posY: 0, active: true, createdAt: '', updatedAt: '' },
    ];

    render(
      <Root />,
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(Client).toHaveBeenCalled());

    expect(capturedOptions?.connectHeaders).toEqual({ Authorization: 'Bearer valid-token' });

    const client = (Client as unknown as ReturnType<typeof vi.fn>).mock.results[0].value as MockClient;
    expect(client.subscribe).toHaveBeenCalledWith(
      '/topic/restaurant/rest-1/tables',
      expect.any(Function)
    );

    await waitFor(() => expect(screen.getByTestId('first-status')).toHaveTextContent('AVAILABLE'));

    server.use(
      http.get('http://localhost:8080/tables', () => {
        return HttpResponse.json(updatedTables);
      })
    );

    subscribeCallback?.({ body: JSON.stringify({ tableId: 'table-1' }) });

    await waitFor(() => expect(screen.getByTestId('first-status')).toHaveTextContent('OCCUPIED'));
  });

  it('deactivates the client on unmount', async () => {
    useAuthStore.setState({
      token: 'valid-token',
      user: null,
      currentRestaurant: null,
      expiresAt: null,
      isAuthenticated: true,
    });
    useTenantStore.setState({
      currentRestaurantId: 'rest-1',
      currentRole: null,
      availableRoles: [],
    });

    const { unmount } = render(
      <Root />,
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(Client).toHaveBeenCalled());
    const client = (Client as unknown as ReturnType<typeof vi.fn>).mock.results[0].value as MockClient;

    unmount();

    await waitFor(() => expect(client.deactivate).toHaveBeenCalled());
  });
});
