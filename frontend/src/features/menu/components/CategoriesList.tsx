import { useCategories } from '@/hooks/useCategories';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';

export function CategoriesList() {
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
      <h2 className="text-lg font-semibold text-neutral-900">Categories</h2>
      {data.length === 0 ? (
        <p className="text-neutral-500">No categories found.</p>
      ) : (
        <ul className="divide-y divide-neutral-200 rounded-lg border border-neutral-200 bg-white">
          {data.map((category) => (
            <li key={category.id} className="p-4">
              <div className="font-medium text-neutral-900">{category.name}</div>
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
