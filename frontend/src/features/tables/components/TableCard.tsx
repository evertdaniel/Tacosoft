import { useState } from 'react';
import { TableDto, TableStatus } from '@/types/domain.types';
import { Button } from '@/components/ui/Button';
import { TableStatusBadge } from './TableStatusBadge';

interface TableCardProps {
  table: TableDto;
  onUpdate: (id: string, status: TableStatus) => void;
}

const statusOptions: TableStatus[] = ['AVAILABLE', 'OCCUPIED', 'RESERVED', 'CLEANING'];

export function TableCard({ table, onUpdate }: TableCardProps) {
  const [selectedStatus, setSelectedStatus] = useState<TableStatus>(table.status);

  const handleUpdate = () => {
    if (selectedStatus !== table.status) {
      onUpdate(table.id, selectedStatus);
    }
  };

  return (
    <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm" data-testid="table-card">
      <div className="flex items-start justify-between">
        <div>
          <h3 className="text-lg font-semibold text-neutral-900">Table {table.num}</h3>
          <p className="text-sm text-neutral-500">{table.seats} seats</p>
        </div>
        <TableStatusBadge status={table.status} />
      </div>

      <div className="mt-4 flex items-center gap-2">
        <label htmlFor={`status-${table.id}`} className="sr-only">Status</label>
        <select
          id={`status-${table.id}`}
          value={selectedStatus}
          onChange={(event) => setSelectedStatus(event.target.value as TableStatus)}
          className="block rounded-md border border-neutral-300 bg-white px-3 py-2 text-sm text-neutral-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        >
          {statusOptions.map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </select>
        <Button onClick={handleUpdate} disabled={selectedStatus === table.status}>
          Update
        </Button>
      </div>
    </div>
  );
}
