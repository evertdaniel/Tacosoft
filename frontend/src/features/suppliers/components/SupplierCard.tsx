import { SupplierDto } from '@/types/domain.types';
import { Button } from '@/components/ui/Button';

interface SupplierCardProps {
  supplier: SupplierDto;
  onEdit: (id: string) => void;
  onToggle: (id: string) => void;
}

export function SupplierCard({ supplier, onEdit, onToggle }: SupplierCardProps) {
  return (
    <div
      className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm"
      data-testid="supplier-card"
    >
      <div className="flex items-start justify-between">
        <div>
          <h3 className="text-lg font-semibold text-neutral-900">{supplier.name}</h3>
          <p className="text-sm text-neutral-500">{supplier.contactName}</p>
          <p className="text-sm text-neutral-500">{supplier.email}</p>
          <p className="text-sm text-neutral-500">{supplier.phone}</p>
        </div>
        <span
          className={`rounded-full px-2.5 py-1 text-xs font-medium ${
            supplier.isActive
              ? 'bg-green-100 text-green-800'
              : 'bg-neutral-100 text-neutral-600'
          }`}
        >
          {supplier.isActive ? 'Active' : 'Inactive'}
        </span>
      </div>
      <div className="mt-4 flex items-center justify-between text-sm text-neutral-500">
        <div className="space-y-1">
          <p>{supplier.address}</p>
          <p>Tax ID: {supplier.taxId}</p>
        </div>
      </div>
      <div className="mt-4 flex justify-end gap-3">
        <Button
          variant="secondary"
          onClick={() => onEdit(supplier.id)}
          data-testid="edit-supplier-button"
        >
          Edit
        </Button>
        <Button
          variant={supplier.isActive ? 'danger' : 'primary'}
          onClick={() => onToggle(supplier.id)}
          data-testid="toggle-supplier-button"
        >
          {supplier.isActive ? 'Deactivate' : 'Activate'}
        </Button>
      </div>
    </div>
  );
}
