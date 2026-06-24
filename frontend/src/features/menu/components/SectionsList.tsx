import { useSections } from '@/hooks/useSections';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import type { SectionDto } from '@/types/domain.types';

interface SectionsListProps {
  onAdd: () => void;
  onEdit: (section: SectionDto) => void;
  onDelete: (id: string) => void;
}

export function SectionsList({ onAdd, onEdit, onDelete }: SectionsListProps) {
  const { data, isLoading, isError, error, refetch } = useSections();

  if (isLoading) {
    return <Loading message="Loading sections…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Sections unavailable"
        message={error?.message ?? 'Failed to load sections. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-neutral-900">Sections</h2>
        <Button onClick={onAdd}>Add Section</Button>
      </div>
      {data.length === 0 ? (
        <p className="text-neutral-500">No sections found.</p>
      ) : (
        <ul className="divide-y divide-neutral-200 rounded-lg border border-neutral-200 bg-white">
          {data.map((section) => (
            <li key={section.id} className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <span className="font-medium text-neutral-900">{section.name}</span>
                  <span className="ml-2 text-xs text-neutral-500">Order {section.displayOrder}</span>
                </div>
                <div className="flex gap-2">
                  <Button variant="secondary" onClick={() => onEdit(section)}>
                    Edit
                  </Button>
                  <Button variant="danger" onClick={() => onDelete(section.id)}>
                    Delete
                  </Button>
                </div>
              </div>
              {section.description && (
                <p className="mt-1 text-sm text-neutral-500">{section.description}</p>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
