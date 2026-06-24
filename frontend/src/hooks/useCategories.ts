import { useQuery } from '@tanstack/react-query';
import { getCategories } from '@/api/menu.api';

export function useCategories() {
  return useQuery({
    queryKey: ['menu', 'categories'],
    queryFn: getCategories,
  });
}
