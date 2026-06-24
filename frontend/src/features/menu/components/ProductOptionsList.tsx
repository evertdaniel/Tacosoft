import { useProductOptions } from '@/hooks/useProductOptions';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { formatCurrency } from '@/utils/formatters';

export function ProductOptionsList() {
  const { data, isLoading, isError, error, refetch } = useProductOptions();

  if (isLoading) {
    return <Loading message="Loading options…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Product options unavailable"
        message={error?.message ?? 'Failed to load product options. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold text-neutral-900">Product Options</h2>
      {data.length === 0 ? (
        <p className="text-neutral-500">No options found.</p>
      ) : (
        <ul className="divide-y divide-neutral-200 rounded-lg border border-neutral-200 bg-white">
          {data.map((option) => (
            <li key={option.id} className="p-4">
              <div className="flex items-center justify-between">
                <span className="font-medium text-neutral-900">{option.name}</span>
                <span className="text-sm text-neutral-700">
                  {option.priceAdjustment > 0 ? `+${formatCurrency(option.priceAdjustment)}` : 'Included'}
                </span>
              </div>
              {option.description && (
                <p className="mt-1 text-sm text-neutral-500">{option.description}</p>
              )}
              <div className="mt-2 flex gap-3 text-xs text-neutral-500">
                <span>{option.isDefault ? 'Default' : 'Optional'}</span>
                <span>{option.isAvailable ? 'Available' : 'Unavailable'}</span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
