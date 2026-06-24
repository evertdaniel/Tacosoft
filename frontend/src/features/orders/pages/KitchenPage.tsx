import { useOrders } from '@/hooks/useOrders';
import { useProducts } from '@/hooks/useProducts';
import { useProductionAreas } from '@/hooks/useProductionAreas';
import { useUpdateOrderDetailStatus } from '@/hooks/useUpdateOrderDetailStatus';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { KitchenOrderCard, KitchenItem } from '../components/KitchenOrderCard';
import { OrderDetailDto, OrderDto, ProductDto, OrderDetailStatus } from '@/types/domain.types';

function isActiveDetail(detail: OrderDetailDto): boolean {
  return detail.status !== 'DELIVERED' && detail.status !== 'CANCELLED';
}

function buildKitchenItems(orders: OrderDto[], products: ProductDto[] | undefined): KitchenItem[] {
  const productMap = new Map(products?.map((product) => [product.id, product.productionAreaId]));

  return orders.flatMap((order) =>
    order.details
      .filter(isActiveDetail)
      .map((detail) => ({
        detail,
        order,
        productionAreaId: productMap.get(detail.productId) ?? null,
      }))
  );
}

function groupByArea(items: KitchenItem[]): Record<string, KitchenItem[]> {
  return items.reduce((groups, item) => {
    const key = item.productionAreaId ?? 'unknown';
    if (!groups[key]) {
      groups[key] = [];
    }
    groups[key].push(item);
    return groups;
  }, {} as Record<string, KitchenItem[]>);
}

export function KitchenPage() {
  const {
    data: orders,
    isLoading: ordersLoading,
    isError: ordersError,
    error: ordersErrorObj,
    refetch: refetchOrders,
  } = useOrders();
  const { data: products, isLoading: productsLoading } = useProducts();
  const { data: productionAreas, isLoading: areasLoading } = useProductionAreas();
  const updateStatus = useUpdateOrderDetailStatus();

  const isLoading = ordersLoading || productsLoading || areasLoading;

  function handleUpdateStatus(detailId: string, status: OrderDetailStatus) {
    updateStatus.mutate({ id: detailId, body: { status } });
  }

  if (isLoading) {
    return <Loading message="Loading kitchen…" />;
  }

  if (ordersError || !orders) {
    return (
      <ErrorState
        title="Kitchen unavailable"
        message={ordersErrorObj?.message ?? 'Failed to load kitchen. Please try again.'}
        onRetry={() => refetchOrders()}
      />
    );
  }

  const items = buildKitchenItems(orders, products);
  const grouped = groupByArea(items);
  const areaMap = new Map(productionAreas?.map((area) => [area.id, area.name]));

  const sortedAreaIds = Object.keys(grouped).sort((a, b) => {
    const nameA = areaMap.get(a) ?? a;
    const nameB = areaMap.get(b) ?? b;
    return nameA.localeCompare(nameB);
  });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Kitchen</h1>

      {items.length === 0 ? (
        <p className="text-neutral-500">No active items.</p>
      ) : (
        <div className="grid gap-6 md:grid-cols-2">
          {sortedAreaIds.map((areaId) => (
            <KitchenOrderCard
              key={areaId}
              areaName={areaMap.get(areaId) ?? 'Unknown area'}
              items={grouped[areaId]}
              onUpdateStatus={handleUpdateStatus}
              isUpdating={updateStatus.isPending}
            />
          ))}
        </div>
      )}
    </div>
  );
}
