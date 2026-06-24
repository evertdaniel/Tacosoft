import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createOrder } from '@/api/orders.api';
import type { OrderDto, CreateOrderBody } from '@/types/domain.types';

export function useCreateOrder() {
  const queryClient = useQueryClient();

  return useMutation<OrderDto, Error, CreateOrderBody>({
    mutationFn: createOrder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
}
