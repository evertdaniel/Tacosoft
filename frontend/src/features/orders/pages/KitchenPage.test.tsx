import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import userEvent from '@testing-library/user-event';
import { KitchenPage } from './KitchenPage';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';
import { resetAuthStore } from '@/stores/auth.store';
import { resetTenantStore } from '@/stores/tenant.store';
import {
  productionAreasFixture,
  productsFixture,
  ordersFixture,
  orderDetailFixture,
} from '@/test/fixtures';
import type { OrderDto, ProductDto } from '@/types/domain.types';

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

const kitchenProducts: ProductDto[] = [
  { ...productsFixture[0], id: 'product-kitchen', name: 'Kitchen Taco', productionAreaId: 'area-1' },
  { ...productsFixture[0], id: 'product-bar', name: 'Bar Drink', productionAreaId: 'area-2' },
];

const kitchenOrder: OrderDto = {
  ...ordersFixture[0],
  id: 'order-kitchen',
  num: 10,
  details: [
    {
      ...orderDetailFixture,
      id: 'detail-kitchen',
      orderId: 'order-kitchen',
      productId: 'product-kitchen',
      productName: 'Kitchen Taco',
      status: 'PENDING',
      quantity: 2,
    },
    {
      ...orderDetailFixture,
      id: 'detail-bar',
      orderId: 'order-kitchen',
      productId: 'product-bar',
      productName: 'Bar Drink',
      status: 'IN_PROGRESS',
      quantity: 1,
    },
    {
      ...orderDetailFixture,
      id: 'detail-delivered',
      orderId: 'order-kitchen',
      productId: 'product-kitchen',
      productName: 'Kitchen Taco',
      status: 'DELIVERED',
      quantity: 1,
    },
  ],
};

function setupKitchenHandlers() {
  server.use(
    http.get('http://localhost:8080/orders', () => {
      return HttpResponse.json([kitchenOrder]);
    }),
    http.get('http://localhost:8080/products', () => {
      return HttpResponse.json(kitchenProducts);
    }),
    http.get('http://localhost:8080/production-areas', () => {
      return HttpResponse.json(productionAreasFixture);
    })
  );
}

describe('KitchenPage', () => {
  beforeEach(() => {
    resetAuthStore();
    resetTenantStore();
  });

  it('shows a loading state while fetching data', () => {
    setupKitchenHandlers();
    render(<KitchenPage />, { wrapper: createWrapper() });

    expect(screen.getByText(/loading kitchen/i)).toBeInTheDocument();
  });

  it('shows an error state when orders cannot be loaded', async () => {
    server.use(
      http.get('http://localhost:8080/orders', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    render(<KitchenPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/kitchen unavailable/i)).toBeInTheDocument());
  });

  it('groups active order details by production area', async () => {
    setupKitchenHandlers();

    render(<KitchenPage />, { wrapper: createWrapper() });

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: 'Kitchen', level: 2 })).toBeInTheDocument()
    );
    expect(screen.getByRole('heading', { name: 'Bar', level: 2 })).toBeInTheDocument();

    expect(screen.getByText('Kitchen Taco')).toBeInTheDocument();
    expect(screen.getByText('Bar Drink')).toBeInTheDocument();
    expect(screen.getAllByTestId('kitchen-order-item')).toHaveLength(2);
  });

  it('shows an empty state when there are no active items', async () => {
    const completedOrder: OrderDto = {
      ...ordersFixture[0],
      details: [
        { ...orderDetailFixture, status: 'DELIVERED' },
        { ...orderDetailFixture, id: 'detail-cancelled', status: 'CANCELLED' },
      ],
    };

    server.use(
      http.get('http://localhost:8080/orders', () => {
        return HttpResponse.json([completedOrder]);
      }),
      http.get('http://localhost:8080/products', () => {
        return HttpResponse.json(kitchenProducts);
      }),
      http.get('http://localhost:8080/production-areas', () => {
        return HttpResponse.json(productionAreasFixture);
      })
    );

    render(<KitchenPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText(/no active items/i)).toBeInTheDocument());
  });

  it('triggers a status update when the kitchen card update button is clicked', async () => {
    let patchCalled = false;

    server.use(
      http.get('http://localhost:8080/orders', () => {
        return HttpResponse.json([kitchenOrder]);
      }),
      http.get('http://localhost:8080/products', () => {
        return HttpResponse.json(kitchenProducts);
      }),
      http.get('http://localhost:8080/production-areas', () => {
        return HttpResponse.json(productionAreasFixture);
      }),
      http.patch('http://localhost:8080/order-details/:id/status', async ({ params }) => {
        patchCalled = true;
        const detail = kitchenOrder.details.find((d) => d.id === params.id) ?? kitchenOrder.details[0];
        return HttpResponse.json({ ...detail, status: 'IN_PROGRESS' });
      })
    );

    render(<KitchenPage />, { wrapper: createWrapper() });

    await waitFor(() => expect(screen.getByText('Kitchen Taco')).toBeInTheDocument());

    const kitchenItem = screen.getByText('Kitchen Taco').closest('li') as HTMLElement;
    const select = within(kitchenItem).getByLabelText('Status');
    await userEvent.selectOptions(select, 'IN_PROGRESS');
    await userEvent.click(within(kitchenItem).getByRole('button', { name: /update/i }));

    await waitFor(() => expect(patchCalled).toBe(true));
  });
});
