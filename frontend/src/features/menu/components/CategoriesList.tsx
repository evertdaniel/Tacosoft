import { useCategories } from '@/hooks/useCategories';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import type { CategoryDto } from '@/types/domain.types';

interface CategoriesListProps {
  onAdd: () => void;
  onEdit: (category: CategoryDto) => void;
  onDelete: (id: string) => void;
}

export function CategoriesList({ onAdd, onEdit, onDelete }: CategoriesListProps) {
  const { data, isLoading, isError, error, refetch } = useCategories();

  if (isLoading) {
    return <Loading message="Loading categories…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Categories unavailable"
        message={error?.message ?? 'Failed to load categories. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-neutral-900">Categories</h2>
        <Button onClick={onAdd}>Add Category</Button>
      </div>
      {data.length === 0 ? (
        <p className="text-neutral-500">No categories found.</p>
      ) : (
        <ul className="divide-y divide-neutral-200 rounded-lg border border-neutral-200 bg-white">
          {data.map((category) => (
            <li key={category.id} className="p-4">
              <div className="flex items-center justify-between">
                <div className="font-medium text-neutral-900">{category.name}</div>
                <div className="flex gap-2">
                  <Button variant="secondary" onClick={() => onEdit(category)}>
                    Edit
                  </Button>
                  <Button variant="danger" onClick={() => onDelete(category.id)}>
                    Delete
                  </Button>
                </div>
              </div>
              {category.description && (
                <p className="mt-1 text-sm text-neutral-500">{category.description}</p>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
