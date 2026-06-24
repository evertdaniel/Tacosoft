import { describe, it, expect } from 'vitest';
import { getOrders, getOrder, createOrder, updateOrderDetailStatus } from './orders.api';
import { ordersFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

describe('orders API', () => {
  it('getOrders returns the list of orders', async () => {
    const orders = await getOrders();

    expect(orders).toEqual(ordersFixture);
  });

  it('getOrder returns a single order by id', async () => {
    const order = await getOrder('order-2');

    expect(order.id).toBe('order-2');
    expect(order.type).toBe('TAKE_AWAY');
  });

  it('createOrder sends the body and returns the created order', async () => {
    const body = {
      type: 'IN_PLACE' as const,
      people: 3,
      tableId: 'table-1',
      details: [{ productId: 'product-1', quantity: 1 }],
    };

    const order = await createOrder(body);

    expect(order.id).toBe('order-new');
    expect(order.type).toBe('IN_PLACE');
    expect(order.people).toBe(3);
  });

  it('updateOrderDetailStatus sends the new status and returns the updated detail', async () => {
    const updated = await updateOrderDetailStatus('detail-1', { status: 'READY' });

    expect(updated.id).toBe('detail-1');
    expect(updated.status).toBe('READY');
  });

  it('createOrder throws when the request fails', async () => {
    server.use(
      http.post('http://localhost:8080/orders', () => {
        return new HttpResponse(JSON.stringify({ message: 'Invalid order' }), { status: 400 });
      })
    );

    await expect(
      createOrder({
        type: 'IN_PLACE',
        people: 1,
        details: [{ productId: 'product-1', quantity: 1 }],
      })
    ).rejects.toThrow('Invalid order');
  });

  it('updateOrderDetailStatus throws when the request fails', async () => {
    server.use(
      http.patch('http://localhost:8080/order-details/:id/status', () => {
        return new HttpResponse(JSON.stringify({ message: 'Invalid transition' }), { status: 409 });
      })
    );

    await expect(updateOrderDetailStatus('detail-1', { status: 'DELIVERED' })).rejects.toThrow('Invalid transition');
  });
});
