import api from './axios';
import { AxiosError } from 'axios';
import type {
  SupplierDto,
  CreateSupplierBody,
  UpdateSupplierBody,
} from '@/types/domain.types';

function getErrorMessage(error: unknown): string {
  const axiosError = error as AxiosError<{ message?: string }>;
  return axiosError.response?.data?.message ?? axiosError.message;
}

export async function getSuppliers(): Promise<SupplierDto[]> {
  try {
    const { data } = await api.get<SupplierDto[]>('/suppliers');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getSupplier(id: string): Promise<SupplierDto> {
  try {
    const { data } = await api.get<SupplierDto>(`/suppliers/${id}`);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function searchSuppliers(query: string): Promise<SupplierDto[]> {
  try {
    const { data } = await api.get<SupplierDto[]>('/suppliers/search', {
      params: { name: query },
    });
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function createSupplier(body: CreateSupplierBody): Promise<SupplierDto> {
  try {
    const { data } = await api.post<SupplierDto>('/suppliers', body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function updateSupplier(id: string, body: UpdateSupplierBody): Promise<SupplierDto> {
  try {
    const { data } = await api.put<SupplierDto>(`/suppliers/${id}`, body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function deactivateSupplier(id: string): Promise<SupplierDto> {
  return updateSupplier(id, { isActive: false });
}

export async function activateSupplier(id: string): Promise<SupplierDto> {
  return updateSupplier(id, { isActive: true });
}
