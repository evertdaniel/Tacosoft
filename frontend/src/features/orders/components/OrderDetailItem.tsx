import { useState } from 'react';
import { OrderDetailDto, OrderDetailStatus } from '@/types/domain.types';
import { Button } from '@/components/ui/Button';

interface OrderDetailItemProps {
  detail: OrderDetailDto;
  onUpdateStatus: (id: string, status: OrderDetailStatus) => void;
}

const statusOptions: OrderDetailStatus[] = ['PENDING', 'IN_PROGRESS', 'READY', 'DELIVERED', 'CANCELLED'];

export function OrderDetailItem({ detail, onUpdateStatus }: OrderDetailItemProps) {
  const [selectedStatus, setSelectedStatus] = useState<OrderDetailStatus>(detail.status);

  const handleUpdate = () => {
    if (selectedStatus !== detail.status) {
      onUpdateStatus(detail.id, selectedStatus);
    }
  };

  return (
    <li className="rounded-lg border border-neutral-200 bg-white p-4" data-testid="order-detail-item">
      <div className="flex items-start justify-between">
        <div>
          <h4 className="font-medium text-neutral-900">{detail.productName}</h4>
          {detail.productOptionName && (
            <p className="text-sm text-neutral-500">{detail.productOptionName}</p>
          )}
          {detail.notes && (
            <p className="text-sm text-neutral-500">Note: {detail.notes}</p>
          )}
        </div>
        <span className="text-sm font-medium text-neutral-900">
          x{detail.quantity} · ${detail.amount.toFixed(2)}
        </span>
      </div>
      <div className="mt-4 flex items-center gap-2">
        <label htmlFor={`status-${detail.id}`} className="sr-only">Status</label>
        <select
          id={`status-${detail.id}`}
          value={selectedStatus}
          onChange={(event) => setSelectedStatus(event.target.value as OrderDetailStatus)}
          className="block rounded-md border border-neutral-300 bg-white px-3 py-2 text-sm text-neutral-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        >
          {statusOptions.map((status) => (
            <option key={status} value={status}>{status.replace('_', ' ')}</option>
          ))}
        </select>
        <Button onClick={handleUpdate} disabled={selectedStatus === detail.status}>
          Update
        </Button>
      </div>
    </li>
  );
}
