import { useState, FormEvent } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import type { OpenCashRegisterBody } from '@/types/domain.types';

interface OpenCashRegisterFormProps {
  onSubmit: (body: OpenCashRegisterBody) => void;
  onCancel: () => void;
  isSubmitting: boolean;
}

export function OpenCashRegisterForm({ onSubmit, onCancel, isSubmitting }: OpenCashRegisterFormProps) {
  const [amount, setAmount] = useState('');
  const [error, setError] = useState<string | null>(null);

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    const parsedAmount = Number(amount);
    if (!amount || Number.isNaN(parsedAmount)) {
      setError('Amount is required');
      return;
    }

    if (parsedAmount < 0) {
      setError('Amount must be zero or positive');
      return;
    }

    onSubmit({ openingAmount: parsedAmount });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        id="opening-amount"
        type="number"
        label="Initial amount"
        value={amount}
        onChange={(event) => setAmount(event.target.value)}
        disabled={isSubmitting}
        error={error ?? undefined}
        placeholder="0"
      />

      <div className="flex justify-end gap-3 pt-2">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Opening…' : 'Open register'}
        </Button>
      </div>
    </form>
  );
}
