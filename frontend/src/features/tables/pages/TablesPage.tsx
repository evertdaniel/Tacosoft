import { useTables } from '@/hooks/useTables';
import { useUpdateTableStatus } from '@/hooks/useUpdateTableStatus';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { TableCard } from '../components/TableCard';

export function TablesPage() {
  const { data, isLoading, isError, error, refetch } = useTables();
  const updateStatus = useUpdateTableStatus();

  if (isLoading) {
    return <Loading message="Loading tables…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Tables unavailable"
        message={error?.message ?? 'Failed to load tables. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Tables</h1>
      {data.length === 0 ? (
        <p className="text-neutral-500">No tables found.</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((table) => (
            <TableCard
              key={table.id}
              table={table}
              onUpdate={(id, status) => updateStatus.mutate({ id, status })}
            />
          ))}
        </div>
      )}
    </div>
  );
}
