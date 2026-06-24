import { useDashboardReport } from '@/hooks/useDashboardReport';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { KpiGrid } from '../components/KpiGrid';

export function DashboardPage() {
  const { data, isLoading, isError, error, refetch } = useDashboardReport();

  if (isLoading) {
    return <Loading message="Loading dashboard…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Dashboard unavailable"
        message={error?.message ?? 'Failed to load dashboard. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Dashboard</h1>
      <KpiGrid report={data} />
    </div>
  );
}
