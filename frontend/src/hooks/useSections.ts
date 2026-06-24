import { useQuery } from '@tanstack/react-query';
import { getSections } from '@/api/menu.api';

export function useSections() {
  return useQuery({
    queryKey: ['menu', 'sections'],
    queryFn: getSections,
  });
}
