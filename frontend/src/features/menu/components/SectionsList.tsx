import { useSections } from '@/hooks/useSections';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';

export function SectionsList() {
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
      <h2 className="text-lg font-semibold text-neutral-900">Sections</h2>
      {data.length === 0 ? (
        <p className="text-neutral-500">No sections found.</p>
      ) : (
        <ul className="divide-y divide-neutral-200 rounded-lg border border-neutral-200 bg-white">
          {data.map((section) => (
            <li key={section.id} className="p-4">
              <div className="flex items-center justify-between">
                <span className="font-medium text-neutral-900">{section.name}</span>
                <span className="text-xs text-neutral-500">Order {section.displayOrder}</span>
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
