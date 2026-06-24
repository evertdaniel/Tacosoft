import { useQuery } from '@tanstack/react-query';
import { getSalesReport, getProductReport, getFinancialReport } from '@/api/reports.api';
import type { ReportDateRangeParams } from '@/types/domain.types';

export function useSalesReport(params: ReportDateRangeParams) {
  return useQuery({
    queryKey: ['reports', 'sales', params],
    queryFn: () => getSalesReport(params),
    enabled: Boolean(params.startDate) && Boolean(params.endDate),
  });
}

export function useProductReport(params: ReportDateRangeParams) {
  return useQuery({
    queryKey: ['reports', 'products', params],
    queryFn: () => getProductReport(params),
    enabled: Boolean(params.startDate) && Boolean(params.endDate),
  });
}

export function useFinancialReport(params: ReportDateRangeParams) {
  return useQuery({
    queryKey: ['reports', 'finances', params],
    queryFn: () => getFinancialReport(params),
    enabled: Boolean(params.startDate) && Boolean(params.endDate),
  });
}
