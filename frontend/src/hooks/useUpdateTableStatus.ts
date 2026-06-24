import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateTableStatus } from '@/api/tables.api';
import { TableStatus } from '@/types/domain.types';

interface UpdateTableStatusVariables {
  id: string;
  status: TableStatus;
}

export function useUpdateTableStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, status }: UpdateTableStatusVariables) => updateTableStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tables'] });
    },
  });
}
