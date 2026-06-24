import api from './axios';
import { AxiosError } from 'axios';
import type { OrderDto, CreateOrderBody, OrderDetailDto, UpdateOrderDetailStatusBody } from '@/types/domain.types';

function getErrorMessage(error: unknown): string {
  const axiosError = error as AxiosError<{ message?: string }>;
  return axiosError.response?.data?.message ?? axiosError.message;
}

export async function getOrders(): Promise<OrderDto[]> {
  try {
    const { data } = await api.get<OrderDto[]>('/orders');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getOrder(id: string): Promise<OrderDto> {
  try {
    const { data } = await api.get<OrderDto>(`/orders/${id}`);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function createOrder(body: CreateOrderBody): Promise<OrderDto> {
  try {
    const { data } = await api.post<OrderDto>('/orders', body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function updateOrderDetailStatus(id: string, body: UpdateOrderDetailStatusBody): Promise<OrderDetailDto> {
  try {
    const { data } = await api.patch<OrderDetailDto>(`/order-details/${id}/status`, body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}
