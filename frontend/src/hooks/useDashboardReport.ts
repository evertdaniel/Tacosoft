import { useQuery } from '@tanstack/react-query';
import { getDashboardReport } from '@/api/reports.api';

export function useDashboardReport() {
  return useQuery({
    queryKey: ['dashboardReport'],
    queryFn: getDashboardReport,
  });
}
