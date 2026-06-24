import { useQuery } from '@tanstack/react-query';
import { getTables } from '@/api/tables.api';

export function useTables() {
  return useQuery({
    queryKey: ['tables'],
    queryFn: getTables,
  });
}
