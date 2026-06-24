import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore, resetAuthStore } from '@/stores/auth.store';
import { useTenantStore, resetTenantStore } from '@/stores/tenant.store';
import { Client } from '@stomp/stompjs';
import { useTables } from './useTables';
import { useOrders } from './useOrders';
import { useOrder } from './useOrder';
import { useWebSocket } from './useWebSocket';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';
import { TableDto } from '@/types/domain.types';
import { ordersFixture } from '@/test/fixtures';

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn(),
}));

vi.mock('sockjs-client', () => ({
  default: vi.fn(() => ({})),
}));

const subscribeCallbacks: Record<string, (message: { body: string }) => void> = {};
let capturedOptions: Record<string, unknown> | null = null;

type MockClient = {
  activate: ReturnType<typeof vi.fn>;
  deactivate: ReturnType<typeof vi.fn>;
  subscribe: ReturnType<typeof vi.fn>;
};

function createMockClient(options: Record<string, unknown>): MockClient {
  capturedOptions = options;
  Object.keys(subscribeCallbacks).forEach((key) => delete subscribeCallbacks[key]);
  return {
    activate: vi.fn(() => {
      if (typeof options.onConnect === 'function') {
        (options.onConnect as () => void)();
      }
    }),
    deactivate: vi.fn(),
    subscribe: vi.fn((destination: string, callback) => {
      subscribeCallbacks[destination] = callback;
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

function TablesStatusLabel() {
  const { data } = useTables();
  return <span data-testid="first-status">{data?.[0]?.status ?? 'loading'}</span>;
}

function OrdersStatusLabel() {
  const { data } = useOrders();
  return <span data-testid="first-order-status">{data?.[0]?.status ?? 'loading'}</span>;
}

function OrderDetailStatusLabel({ id }: { id: string }) {
  const { data } = useOrder(id);
  return <span data-testid="order-status">{data?.status ?? 'loading'}</span>;
}

function TablesRoot() {
  useWebSocket();
  return <TablesStatusLabel />;
}

function OrdersRoot() {
  useWebSocket();
  return <OrdersStatusLabel />;
}

function OrderDetailRoot({ id }: { id: string }) {
  useWebSocket(id);
  return <OrderDetailStatusLabel id={id} />;
}

describe('useWebSocket', () => {
  beforeEach(() => {
    resetAuthStore();
    resetTenantStore();
    (Client as unknown as ReturnType<typeof vi.fn>).mockReset();
    (Client as unknown as ReturnType<typeof vi.fn>).mockImplementation(createMockClient);
  });

  it('does not connect when there is no auth token', () => {
    render(<TablesRoot />, { wrapper: createWrapper() });

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

    render(<TablesRoot />, { wrapper: createWrapper() });

    await waitFor(() => expect(Client).toHaveBeenCalled());

    expect(capturedOptions?.connectHeaders).toEqual({ Authorization: 'Bearer valid-token' });

    const client = (Client as unknown as ReturnType<typeof vi.fn>).mock.results[0].value as MockClient;
    expect(client.subscribe).toHaveBeenCalledWith('/topic/restaurant/rest-1/tables', expect.any(Function));

    await waitFor(() => expect(screen.getByTestId('first-status')).toHaveTextContent('AVAILABLE'));

    server.use(
      http.get('http://localhost:8080/tables', () => {
        return HttpResponse.json(updatedTables);
      })
    );

    subscribeCallbacks['/topic/restaurant/rest-1/tables']?.({ body: JSON.stringify({ tableId: 'table-1' }) });

    await waitFor(() => expect(screen.getByTestId('first-status')).toHaveTextContent('OCCUPIED'));
  });

  it('connects, subscribes to the orders topic, and invalidates orders on message', async () => {
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

    const updatedOrders = [{ ...ordersFixture[0], status: 'IN_PROGRESS' as const }];

    render(<OrdersRoot />, { wrapper: createWrapper() });

    await waitFor(() => expect(Client).toHaveBeenCalled());

    const client = (Client as unknown as ReturnType<typeof vi.fn>).mock.results[0].value as MockClient;
    expect(client.subscribe).toHaveBeenCalledWith('/topic/restaurant/rest-1/orders', expect.any(Function));

    await waitFor(() => expect(screen.getByTestId('first-order-status')).toHaveTextContent('PENDING'));

    server.use(
      http.get('http://localhost:8080/orders', () => {
        return HttpResponse.json(updatedOrders);
      })
    );

    subscribeCallbacks['/topic/restaurant/rest-1/orders']?.({ body: JSON.stringify({ orderId: 'order-1' }) });

    await waitFor(() => expect(screen.getByTestId('first-order-status')).toHaveTextContent('IN_PROGRESS'));
  });

  it('subscribes to the order-specific topic when orderId is provided and invalidates the order query on message', async () => {
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

    const updatedOrder = { ...ordersFixture[0], status: 'IN_PROGRESS' as const };

    render(<OrderDetailRoot id="order-1" />, { wrapper: createWrapper() });

    await waitFor(() => expect(Client).toHaveBeenCalled());

    const client = (Client as unknown as ReturnType<typeof vi.fn>).mock.results[0].value as MockClient;
    expect(client.subscribe).toHaveBeenCalledWith(
      '/topic/restaurant/rest-1/orders/order-1',
      expect.any(Function)
    );

    await waitFor(() => expect(screen.getByTestId('order-status')).toHaveTextContent('PENDING'));

    server.use(
      http.get('http://localhost:8080/orders/order-1', () => {
        return HttpResponse.json(updatedOrder);
      })
    );

    subscribeCallbacks['/topic/restaurant/rest-1/orders/order-1']?.({
      body: JSON.stringify({ orderId: 'order-1' }),
    });

    await waitFor(() => expect(screen.getByTestId('order-status')).toHaveTextContent('IN_PROGRESS'));
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

    const { unmount } = render(<TablesRoot />, { wrapper: createWrapper() });

    await waitFor(() => expect(Client).toHaveBeenCalled());
    const client = (Client as unknown as ReturnType<typeof vi.fn>).mock.results[0].value as MockClient;

    unmount();

    await waitFor(() => expect(client.deactivate).toHaveBeenCalled());
  });
});
