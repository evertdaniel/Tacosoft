import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useOrder } from '@/hooks/useOrder';
import { useUpdateOrderDetailStatus } from '@/hooks/useUpdateOrderDetailStatus';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import { OrderDetailItem } from '../components/OrderDetailItem';
import { formatCurrency } from '@/utils/formatters';
import type { OrderDetailStatus } from '@/types/domain.types';

export function OrderDetailPage() {
  const { id = '' } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const { data, isLoading, isError, error, refetch } = useOrder(id);
  const updateStatus = useUpdateOrderDetailStatus();

  function handleUpdateStatus(detailId: string, status: OrderDetailStatus) {
    setSuccessMessage(null);
    updateStatus.mutate(
      { id: detailId, body: { status } },
      {
        onSuccess: () => {
          setSuccessMessage('Status updated');
        },
      }
    );
  }

  if (isLoading) {
    return <Loading message="Loading order…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Order unavailable"
        message={error?.message ?? 'Failed to load order. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button type="button" variant="secondary" onClick={() => navigate('/orders')}>
          Back
        </Button>
        <h1 className="text-2xl font-bold text-neutral-900">Order #{data.num}</h1>
        <span className="rounded-full bg-blue-100 px-2.5 py-1 text-xs font-medium text-blue-800">
          {data.status}
        </span>
      </div>

      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <p className="text-sm text-neutral-500">Type</p>
            <p className="font-medium text-neutral-900">{data.type === 'IN_PLACE' ? 'In place' : 'Take away'}</p>
          </div>
          <div>
            <p className="text-sm text-neutral-500">People</p>
            <p className="font-medium text-neutral-900">{data.people}</p>
          </div>
          <div>
            <p className="text-sm text-neutral-500">Total</p>
            <p className="font-medium text-neutral-900">{formatCurrency(data.total)}</p>
          </div>
        </div>
      </div>

      {successMessage && (
        <div className="rounded-md bg-green-50 p-4 text-sm text-green-700" role="status">{successMessage}</div>
      )}

      <div className="space-y-4">
        <h2 className="text-lg font-semibold text-neutral-900">Items</h2>
        <ul className="space-y-3">
          {data.details.map((detail) => (
            <OrderDetailItem
              key={detail.id}
              detail={detail}
              onUpdateStatus={handleUpdateStatus}
            />
          ))}
        </ul>
      </div>
    </div>
  );
}
