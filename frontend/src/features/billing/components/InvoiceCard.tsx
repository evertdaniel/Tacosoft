import { InvoiceDto } from '@/types/domain.types';
import { formatCurrency } from '@/utils/formatters';
import { Button } from '@/components/ui/Button';

interface InvoiceCardProps {
  invoice: InvoiceDto;
  onPay: (id: string) => void;
}

export function InvoiceCard({ invoice, onPay }: InvoiceCardProps) {
  return (
    <div
      className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm"
      data-testid="invoice-card"
    >
      <div className="flex items-start justify-between">
        <div>
          <h3 className="text-lg font-semibold text-neutral-900">Folio #{invoice.folio}</h3>
          <p className="text-sm text-neutral-500">Order #{invoice.orderId.split('-')[1]}</p>
          <p className="text-xs text-neutral-400">{new Date(invoice.createdAt).toLocaleString()}</p>
        </div>
        <span
          className={`rounded-full px-2.5 py-1 text-xs font-medium ${
            invoice.isPaid
              ? 'bg-green-100 text-green-800'
              : 'bg-amber-100 text-amber-800'
          }`}
        >
          {invoice.isPaid ? 'Paid' : 'Unpaid'}
        </span>
      </div>
      <div className="mt-4 flex items-center justify-between">
        <div className="space-y-1">
          <p className="text-sm text-neutral-500">Subtotal: {formatCurrency(invoice.subtotal)}</p>
          <p className="text-sm text-neutral-500">Tax: {formatCurrency(invoice.tax)}</p>
          {invoice.isPaid && invoice.paymentMethod && (
            <p className="text-sm text-neutral-500">Method: {invoice.paymentMethod.replace(/_/g, ' ')}</p>
          )}
        </div>
        <span className="text-lg font-semibold text-neutral-900">{formatCurrency(invoice.total)}</span>
      </div>
      {!invoice.isPaid && (
        <div className="mt-4 flex justify-end">
          <Button onClick={() => onPay(invoice.id)} data-testid="pay-button">Pay</Button>
        </div>
      )}
    </div>
  );
}
