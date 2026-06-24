import { useProductionAreas } from '@/hooks/useProductionAreas';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import type { ProductionAreaDto } from '@/types/domain.types';

interface ProductionAreasListProps {
  onAdd: () => void;
  onEdit: (area: ProductionAreaDto) => void;
  onDelete: (id: string) => void;
}

export function ProductionAreasList({ onAdd, onEdit, onDelete }: ProductionAreasListProps) {
  const { data, isLoading, isError, error, refetch } = useProductionAreas();

  if (isLoading) {
    return <Loading message="Loading production areas…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Production areas unavailable"
        message={error?.message ?? 'Failed to load production areas. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-neutral-900">Production Areas</h2>
        <Button onClick={onAdd}>Add Production Area</Button>
      </div>
      {data.length === 0 ? (
        <p className="text-neutral-500">No production areas found.</p>
      ) : (
        <ul className="divide-y divide-neutral-200 rounded-lg border border-neutral-200 bg-white">
          {data.map((area) => (
            <li key={area.id} className="p-4">
              <div className="flex items-center justify-between">
                <div className="font-medium text-neutral-900">{area.name}</div>
                <div className="flex gap-2">
                  <Button variant="secondary" onClick={() => onEdit(area)}>
                    Edit
                  </Button>
                  <Button variant="danger" onClick={() => onDelete(area.id)}>
                    Delete
                  </Button>
                </div>
              </div>
              {area.description && (
                <p className="mt-1 text-sm text-neutral-500">{area.description}</p>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
