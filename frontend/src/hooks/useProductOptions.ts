import { useQuery } from '@tanstack/react-query';
import { getProductOptions } from '@/api/menu.api';

export function useProductOptions() {
  return useQuery({
    queryKey: ['menu', 'productOptions'],
    queryFn: getProductOptions,
  });
}
