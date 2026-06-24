import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useOrders } from '@/hooks/useOrders';
import { useCreateOrder } from '@/hooks/useCreateOrder';
import { useTables } from '@/hooks/useTables';
import { useProducts } from '@/hooks/useProducts';
import { useProductOptions } from '@/hooks/useProductOptions';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import { FormModal } from '@/components/ui/FormModal';
import { OrderCard } from '../components/OrderCard';
import { CreateOrderForm } from '../components/CreateOrderForm';
import type { CreateOrderBody } from '@/types/domain.types';

export function OrdersPage() {
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { data, isLoading, isError, error: listError, refetch } = useOrders();
  const createOrder = useCreateOrder();
  const { data: tables } = useTables();
  const { data: products } = useProducts();
  const { data: productOptions } = useProductOptions();

  function handleOpenModal() {
    setError(null);
    setIsModalOpen(true);
  }

  function handleCloseModal() {
    setIsModalOpen(false);
    setError(null);
  }

  function handleSubmit(body: CreateOrderBody) {
    setError(null);
    createOrder.mutate(body, {
      onSuccess: () => {
        handleCloseModal();
      },
      onError: (err) => {
        setError(err.message);
      },
    });
  }

  if (isLoading) {
    return <Loading message="Loading orders…" />;
  }

  if (isError || !data) {
    return (
      <ErrorState
        title="Orders unavailable"
        message={listError?.message ?? 'Failed to load orders. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-neutral-900">Orders</h1>
        <Button onClick={handleOpenModal}>Add Order</Button>
      </div>

      {error && (
        <div className="rounded-md bg-red-50 p-4 text-sm text-red-700" role="alert">{error}</div>
      )}

      {data.length === 0 ? (
        <p className="text-neutral-500">No orders found.</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((order) => (
            <OrderCard key={order.id} order={order} onClick={(id) => navigate(`/orders/${id}`)} />
          ))}
        </div>
      )}

      <FormModal isOpen={isModalOpen} title="Create Order" onClose={handleCloseModal}>
        <CreateOrderForm
          tables={tables ?? []}
          products={products ?? []}
          productOptions={productOptions ?? []}
          onSubmit={handleSubmit}
          isLoading={createOrder.isPending}
        />
      </FormModal>
    </div>
  );
}
