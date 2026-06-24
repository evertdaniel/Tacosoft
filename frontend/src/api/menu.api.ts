import api from './axios';
import { AxiosError } from 'axios';
import type {
  SectionDto,
  CategoryDto,
  ProductDto,
  ProductOptionDto,
  ProductionAreaDto,
} from '@/types/domain.types';

async function getList<T>(path: string): Promise<T[]> {
  try {
    const { data } = await api.get<T[]>(path);
    return data;
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>;
    const message = axiosError.response?.data?.message ?? axiosError.message;
    throw new Error(message);
  }
}

export function getSections(): Promise<SectionDto[]> {
  return getList<SectionDto>('/sections');
}

export function getCategories(): Promise<CategoryDto[]> {
  return getList<CategoryDto>('/categories');
}

export function getProducts(): Promise<ProductDto[]> {
  return getList<ProductDto>('/products');
}

export function getProductOptions(): Promise<ProductOptionDto[]> {
  return getList<ProductOptionDto>('/product-options');
}

export function getProductionAreas(): Promise<ProductionAreaDto[]> {
  return getList<ProductionAreaDto>('/production-areas');
}
