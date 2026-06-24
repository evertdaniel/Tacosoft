import api from './axios';
import { AxiosError } from 'axios';
import type {
  SectionDto,
  CategoryDto,
  ProductDto,
  ProductOptionDto,
  ProductionAreaDto,
  CreateSectionBody,
  UpdateSectionBody,
  CreateCategoryBody,
  UpdateCategoryBody,
  CreateProductBody,
  UpdateProductBody,
  CreateProductOptionBody,
  UpdateProductOptionBody,
  CreateProductionAreaBody,
  UpdateProductionAreaBody,
} from '@/types/domain.types';

function getErrorMessage(error: unknown): string {
  const axiosError = error as AxiosError<{ message?: string }>;
  return axiosError.response?.data?.message ?? axiosError.message;
}

async function getList<T>(path: string): Promise<T[]> {
  try {
    const { data } = await api.get<T[]>(path);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

async function create<T, B>(path: string, body: B): Promise<T> {
  try {
    const { data } = await api.post<T>(path, body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

async function update<T, B>(path: string, body: B): Promise<T> {
  try {
    const { data } = await api.put<T>(path, body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

async function remove(path: string): Promise<void> {
  try {
    await api.delete(path);
  } catch (error) {
    throw new Error(getErrorMessage(error));
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

export function createSection(body: CreateSectionBody): Promise<SectionDto> {
  return create<SectionDto, CreateSectionBody>('/sections', body);
}

export function updateSection(id: string, body: UpdateSectionBody): Promise<SectionDto> {
  return update<SectionDto, UpdateSectionBody>(`/sections/${id}`, body);
}

export function deleteSection(id: string): Promise<void> {
  return remove(`/sections/${id}`);
}

export function createCategory(body: CreateCategoryBody): Promise<CategoryDto> {
  return create<CategoryDto, CreateCategoryBody>('/categories', body);
}

export function updateCategory(id: string, body: UpdateCategoryBody): Promise<CategoryDto> {
  return update<CategoryDto, UpdateCategoryBody>(`/categories/${id}`, body);
}

export function deleteCategory(id: string): Promise<void> {
  return remove(`/categories/${id}`);
}

export function createProduct(body: CreateProductBody): Promise<ProductDto> {
  return create<ProductDto, CreateProductBody>('/products', body);
}

export function updateProduct(id: string, body: UpdateProductBody): Promise<ProductDto> {
  return update<ProductDto, UpdateProductBody>(`/products/${id}`, body);
}

export function deleteProduct(id: string): Promise<void> {
  return remove(`/products/${id}`);
}

export function createProductOption(body: CreateProductOptionBody): Promise<ProductOptionDto> {
  return create<ProductOptionDto, CreateProductOptionBody>('/product-options', body);
}

export function updateProductOption(id: string, body: UpdateProductOptionBody): Promise<ProductOptionDto> {
  return update<ProductOptionDto, UpdateProductOptionBody>(`/product-options/${id}`, body);
}

export function deleteProductOption(id: string): Promise<void> {
  return remove(`/product-options/${id}`);
}

export function createProductionArea(body: CreateProductionAreaBody): Promise<ProductionAreaDto> {
  return create<ProductionAreaDto, CreateProductionAreaBody>('/production-areas', body);
}

export function updateProductionArea(id: string, body: UpdateProductionAreaBody): Promise<ProductionAreaDto> {
  return update<ProductionAreaDto, UpdateProductionAreaBody>(`/production-areas/${id}`, body);
}

export function deleteProductionArea(id: string): Promise<void> {
  return remove(`/production-areas/${id}`);
}
