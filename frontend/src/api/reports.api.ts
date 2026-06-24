import api from './axios';
import { DashboardReportDto } from '@/types/domain.types';
import { AxiosError } from 'axios';

export async function getDashboardReport(): Promise<DashboardReportDto> {
  try {
    const { data } = await api.get<DashboardReportDto>('/reports/dashboard');
    return data;
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>;
    const message = axiosError.response?.data?.message ?? axiosError.message;
    throw new Error(message);
  }
}
