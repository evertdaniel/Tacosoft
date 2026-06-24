import api from './axios';
import { TableDto, TableStatus } from '@/types/domain.types';
import { AxiosError } from 'axios';

export async function getTables(): Promise<TableDto[]> {
  try {
    const { data } = await api.get<TableDto[]>('/tables');
    return data;
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>;
    const message = axiosError.response?.data?.message ?? axiosError.message;
    throw new Error(message);
  }
}

export async function updateTableStatus(id: string, status: TableStatus): Promise<TableDto> {
  try {
    const { data } = await api.put<TableDto>(`/tables/${id}/status`, { status });
    return data;
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>;
    const message = axiosError.response?.data?.message ?? axiosError.message;
    throw new Error(message);
  }
}
