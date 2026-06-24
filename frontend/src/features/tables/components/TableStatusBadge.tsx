import { TableStatus } from '@/types/domain.types';

interface TableStatusBadgeProps {
  status: TableStatus;
}

const statusLabels: Record<TableStatus, string> = {
  AVAILABLE: 'Available',
  OCCUPIED: 'Occupied',
  RESERVED: 'Reserved',
  CLEANING: 'Cleaning',
};

const statusClasses: Record<TableStatus, string> = {
  AVAILABLE: 'bg-green-100 text-green-800',
  OCCUPIED: 'bg-red-100 text-red-800',
  RESERVED: 'bg-yellow-100 text-yellow-800',
  CLEANING: 'bg-neutral-100 text-neutral-800',
};

export function TableStatusBadge({ status }: TableStatusBadgeProps) {
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${statusClasses[status]}`}
      data-testid="table-status-badge"
    >
      {statusLabels[status]}
    </span>
  );
}
