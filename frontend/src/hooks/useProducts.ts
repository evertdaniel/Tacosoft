import { useQuery } from '@tanstack/react-query';
import { getProducts } from '@/api/menu.api';

export function useProducts() {
  return useQuery({
    queryKey: ['menu', 'products'],
    queryFn: getProducts,
  });
}
