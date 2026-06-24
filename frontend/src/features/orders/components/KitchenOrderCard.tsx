import { useState } from 'react';
import { OrderDetailDto, OrderDto, OrderDetailStatus } from '@/types/domain.types';
import { Button } from '@/components/ui/Button';

export interface KitchenItem {
  detail: OrderDetailDto;
  order: OrderDto;
  productionAreaId?: string | null;
}

interface KitchenOrderCardProps {
  areaName: string;
  items: KitchenItem[];
  onUpdateStatus: (detailId: string, status: OrderDetailStatus) => void;
  isUpdating?: boolean;
}

const statusOptions: OrderDetailStatus[] = ['PENDING', 'IN_PROGRESS', 'READY', 'DELIVERED', 'CANCELLED'];

function statusBadgeClasses(status: OrderDetailStatus): string {
  const base = 'rounded-full px-2.5 py-1 text-xs font-medium';
  switch (status) {
    case 'PENDING':
      return `${base} bg-amber-100 text-amber-800`;
    case 'IN_PROGRESS':
      return `${base} bg-blue-100 text-blue-800`;
    case 'READY':
      return `${base} bg-green-100 text-green-800`;
    case 'DELIVERED':
      return `${base} bg-neutral-100 text-neutral-800`;
    case 'CANCELLED':
      return `${base} bg-red-100 text-red-800`;
    default:
      return `${base} bg-neutral-100 text-neutral-800`;
  }
}

export function KitchenOrderCard({
  areaName,
  items,
  onUpdateStatus,
  isUpdating = false,
}: KitchenOrderCardProps) {
  return (
    <section
      className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm"
      data-testid="kitchen-order-card"
    >
      <h2 className="mb-4 text-lg font-semibold text-neutral-900">{areaName}</h2>
      <ul className="space-y-4">
        {items.map((item) => (
          <KitchenOrderCardItem
            key={item.detail.id}
            item={item}
            onUpdateStatus={onUpdateStatus}
            isUpdating={isUpdating}
          />
        ))}
      </ul>
    </section>
  );
}

interface KitchenOrderCardItemProps {
  item: KitchenItem;
  onUpdateStatus: (detailId: string, status: OrderDetailStatus) => void;
  isUpdating: boolean;
}

function KitchenOrderCardItem({ item, onUpdateStatus, isUpdating }: KitchenOrderCardItemProps) {
  const { detail, order } = item;
  const [selectedStatus, setSelectedStatus] = useState<OrderDetailStatus>(detail.status);

  function handleUpdate() {
    if (selectedStatus !== detail.status) {
      onUpdateStatus(detail.id, selectedStatus);
    }
  }

  return (
    <li
      className="rounded-lg border border-neutral-200 bg-neutral-50 p-4"
      data-testid="kitchen-order-item"
    >
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="rounded bg-neutral-200 px-1.5 py-0.5 text-xs font-medium text-neutral-700">
              Order #{order.num}
            </span>
            <span className={statusBadgeClasses(detail.status)}>{detail.status.replace('_', ' ')}</span>
          </div>
          <h3 className="font-medium text-neutral-900">{detail.productName}</h3>
          {detail.productOptionName && (
            <p className="text-sm text-neutral-500">{detail.productOptionName}</p>
          )}
          {detail.notes && (
            <p className="text-sm text-neutral-500">Note: {detail.notes}</p>
          )}
        </div>
        <span className="text-sm font-medium text-neutral-900">x{detail.quantity}</span>
      </div>
      <div className="mt-4 flex items-center gap-2">
        <label htmlFor={`kitchen-status-${detail.id}`} className="sr-only">
          Status
        </label>
        <select
          id={`kitchen-status-${detail.id}`}
          value={selectedStatus}
          onChange={(event) => setSelectedStatus(event.target.value as OrderDetailStatus)}
          className="block rounded-md border border-neutral-300 bg-white px-3 py-2 text-sm text-neutral-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          disabled={isUpdating}
        >
          {statusOptions.map((status) => (
            <option key={status} value={status}>
              {status.replace('_', ' ')}
            </option>
          ))}
        </select>
        <Button onClick={handleUpdate} disabled={selectedStatus === detail.status || isUpdating}>
          Update
        </Button>
      </div>
    </li>
  );
}
