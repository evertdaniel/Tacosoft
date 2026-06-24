import { useQuery } from '@tanstack/react-query';
import { getOrder } from '@/api/orders.api';

export function useOrder(id: string) {
  return useQuery({
    queryKey: ['orders', id],
    queryFn: () => getOrder(id),
    enabled: Boolean(id),
  });
}
