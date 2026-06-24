import { describe, it, expect } from 'vitest';
import { getInvoices, getUnpaidInvoices, payInvoice } from './billing.api';
import { invoicesFixture, unpaidInvoicesFixture, paymentBodyFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

describe('billing API', () => {
  it('getInvoices returns the list of invoices', async () => {
    const invoices = await getInvoices();

    expect(invoices).toEqual(invoicesFixture);
  });

  it('getUnpaidInvoices returns only unpaid invoices', async () => {
    const invoices = await getUnpaidInvoices();

    expect(invoices).toEqual(unpaidInvoicesFixture);
    expect(invoices.every((invoice) => !invoice.isPaid)).toBe(true);
  });

  it('payInvoice sends the body and returns the updated invoice', async () => {
    const paid = await payInvoice('invoice-1', paymentBodyFixture);

    expect(paid.id).toBe('invoice-1');
    expect(paid.isPaid).toBe(true);
    expect(paid.paymentMethod).toBe('CASH');
  });

  it('payInvoice throws when the request fails', async () => {
    server.use(
      http.post('http://localhost:8080/invoices/:id/pay', () => {
        return new HttpResponse(JSON.stringify({ message: 'Payment failed' }), { status: 400 });
      })
    );

    await expect(payInvoice('invoice-1', paymentBodyFixture)).rejects.toThrow('Payment failed');
  });

  it('getInvoices throws when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/invoices', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    await expect(getInvoices()).rejects.toThrow('Server error');
  });
});
