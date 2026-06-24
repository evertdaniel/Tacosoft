import { useState, FormEvent, useEffect } from 'react';
import { InvoiceDto, PaymentBody, PaymentMethod } from '@/types/domain.types';
import { FormModal } from '@/components/ui/FormModal';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { formatCurrency } from '@/utils/formatters';

interface PaymentModalProps {
  isOpen: boolean;
  invoice: InvoiceDto | null;
  onClose: () => void;
  onSubmit: (body: PaymentBody) => void;
  isSubmitting: boolean;
}

const paymentMethods: { value: PaymentMethod; label: string }[] = [
  { value: 'CASH', label: 'Cash' },
  { value: 'CREDIT_CARD', label: 'Credit card' },
  { value: 'TRANSFER', label: 'Transfer' },
];

export function PaymentModal({ isOpen, invoice, onClose, onSubmit, isSubmitting }: PaymentModalProps) {
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CASH');
  const [amount, setAmount] = useState<string>(invoice ? String(invoice.total) : '');
  const [referenceId, setReferenceId] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (invoice) {
      setAmount(String(invoice.total));
      setError(null);
    }
  }, [invoice]);

  if (!invoice) return null;

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    const parsedAmount = Number(amount);
    if (!amount || Number.isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Amount is required');
      return;
    }

    onSubmit({
      amount: parsedAmount,
      paymentMethod,
      referenceId,
    });
  }

  return (
    <FormModal isOpen={isOpen} title="Record payment" onClose={onClose}>
      <form id="payment-form" onSubmit={handleSubmit} className="space-y-4">
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-600">Folio #{invoice.folio}</p>
          <p className="text-lg font-semibold text-neutral-900">{formatCurrency(invoice.total)}</p>
        </div>

        <div>
          <label htmlFor="payment-method" className="block text-sm font-medium text-neutral-700">
            Payment method
          </label>
          <select
            id="payment-method"
            value={paymentMethod}
            onChange={(event) => setPaymentMethod(event.target.value as PaymentMethod)}
            disabled={isSubmitting}
            className="mt-1 block w-full rounded-md border border-neutral-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:bg-neutral-100"
          >
            {paymentMethods.map((method) => (
              <option key={method.value} value={method.value}>
                {method.label}
              </option>
            ))}
          </select>
        </div>

        <Input
          id="amount"
          type="number"
          label="Amount"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          disabled={isSubmitting}
          error={error ?? undefined}
        />

        <Input
          id="reference-id"
          type="text"
          label="Reference ID"
          value={referenceId}
          onChange={(event) => setReferenceId(event.target.value)}
          disabled={isSubmitting}
          placeholder="Optional"
        />

        <div className="flex justify-end gap-3 pt-2">
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Processing…' : 'Confirm payment'}
          </Button>
        </div>
      </form>
    </FormModal>
  );
}
