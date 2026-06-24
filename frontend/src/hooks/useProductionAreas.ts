import { useQuery } from '@tanstack/react-query';
import { getProductionAreas } from '@/api/menu.api';

export function useProductionAreas() {
  return useQuery({
    queryKey: ['menu', 'productionAreas'],
    queryFn: getProductionAreas,
  });
}
