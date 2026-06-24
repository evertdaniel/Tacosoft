import { OrderDto } from '@/types/domain.types';
import { formatCurrency } from '@/utils/formatters';

interface OrderCardProps {
  order: OrderDto;
  onClick: (id: string) => void;
}

export function OrderCard({ order, onClick }: OrderCardProps) {
  return (
    <button
      type="button"
      onClick={() => onClick(order.id)}
      className="w-full rounded-xl border border-neutral-200 bg-white p-5 text-left shadow-sm transition-shadow hover:shadow-md"
      data-testid="order-card"
    >
      <div className="flex items-start justify-between">
        <div>
          <h3 className="text-lg font-semibold text-neutral-900">Order #{order.num}</h3>
          <p className="text-sm text-neutral-500">
            {order.type === 'IN_PLACE' ? 'In place' : 'Take away'}
            {order.tableId ? ` · Table ${order.tableId}` : ''}
            {' · '}
            {order.people} people
          </p>
        </div>
        <span className="rounded-full bg-blue-100 px-2.5 py-1 text-xs font-medium text-blue-800">
          {order.status}
        </span>
      </div>
      <div className="mt-4 flex items-center justify-between">
        <span className="text-sm text-neutral-500">{order.details.length} items</span>
        <span className="text-lg font-semibold text-neutral-900">{formatCurrency(order.total)}</span>
      </div>
    </button>
  );
}
