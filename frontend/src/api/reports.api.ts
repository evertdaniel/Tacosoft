import api from './axios';
import { AxiosError } from 'axios';
import type {
  DashboardReportDto,
  SalesSummaryDto,
  ProductReportDto,
  FinancialReportDto,
  ReportDateRangeParams,
} from '@/types/domain.types';

function getErrorMessage(error: unknown): string {
  const axiosError = error as AxiosError<{ message?: string }>;
  return axiosError.response?.data?.message ?? axiosError.message;
}

export async function getDashboardReport(): Promise<DashboardReportDto> {
  try {
    const { data } = await api.get<DashboardReportDto>('/reports/dashboard');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getSalesReport(params: ReportDateRangeParams): Promise<SalesSummaryDto> {
  try {
    const { data } = await api.get<SalesSummaryDto>('/reports/sales', {
      params: { startDate: params.startDate, endDate: params.endDate },
    });
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getProductReport(params: ReportDateRangeParams): Promise<ProductReportDto[]> {
  try {
    const { data } = await api.get<ProductReportDto[]>('/reports/products', {
      params: { startDate: params.startDate, endDate: params.endDate },
    });
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getFinancialReport(params: ReportDateRangeParams): Promise<FinancialReportDto> {
  try {
    const { data } = await api.get<FinancialReportDto>('/reports/finances', {
      params: { startDate: params.startDate, endDate: params.endDate },
    });
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}
