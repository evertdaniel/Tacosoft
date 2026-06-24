import { useProducts } from '@/hooks/useProducts';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import { formatCurrency } from '@/utils/formatters';
import type { ProductDto } from '@/types/domain.types';

interface ProductsListProps {
  onAdd: () => void;
  onEdit: (product: ProductDto) => void;
  onDelete: (id: string) => void;
}

export function ProductsList({ onAdd, onEdit, onDelete }: ProductsListProps) {
  const { data, isLoading, isError, error, refetch } = useProducts();

  if (isLoading) {
    return <Loading message="Loading products…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Products unavailable"
        message={error?.message ?? 'Failed to load products. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-neutral-900">Products</h2>
        <Button onClick={onAdd}>Add Product</Button>
      </div>
      {data.length === 0 ? (
        <p className="text-neutral-500">No products found.</p>
      ) : (
        <ul className="divide-y divide-neutral-200 rounded-lg border border-neutral-200 bg-white">
          {data.map((product) => (
            <li key={product.id} className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <span className="font-medium text-neutral-900">{product.name}</span>
                  <span className="ml-2 text-sm font-medium text-neutral-700">{formatCurrency(product.price)}</span>
                </div>
                <div className="flex gap-2">
                  <Button variant="secondary" onClick={() => onEdit(product)}>
                    Edit
                  </Button>
                  <Button variant="danger" onClick={() => onDelete(product.id)}>
                    Delete
                  </Button>
                </div>
              </div>
              {product.description && (
                <p className="mt-1 text-sm text-neutral-500">{product.description}</p>
              )}
              <div className="mt-2 flex gap-3 text-xs text-neutral-500">
                <span>Status: {product.status}</span>
                <span>Stock: {product.stock}</span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
