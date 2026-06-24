import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateOrderDetailStatus } from '@/api/orders.api';
import type { OrderDetailDto, UpdateOrderDetailStatusBody } from '@/types/domain.types';

interface UpdateOrderDetailStatusVariables {
  id: string;
  body: UpdateOrderDetailStatusBody;
}

export function useUpdateOrderDetailStatus() {
  const queryClient = useQueryClient();

  return useMutation<OrderDetailDto, Error, UpdateOrderDetailStatusVariables>({
    mutationFn: ({ id, body }) => updateOrderDetailStatus(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
}
