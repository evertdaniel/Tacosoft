import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  createProduct,
  updateProduct,
  deleteProduct,
} from '@/api/menu.api';
import type {
  ProductDto,
  CreateProductBody,
  UpdateProductBody,
} from '@/types/domain.types';

export function useCreateProduct() {
  const queryClient = useQueryClient();

  return useMutation<ProductDto, Error, CreateProductBody>({
    mutationFn: createProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'products'] });
    },
  });
}

interface UpdateProductVariables {
  id: string;
  body: UpdateProductBody;
}

export function useUpdateProduct() {
  const queryClient = useQueryClient();

  return useMutation<ProductDto, Error, UpdateProductVariables>({
    mutationFn: ({ id, body }) => updateProduct(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'products'] });
    },
  });
}

export function useDeleteProduct() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: deleteProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'products'] });
    },
  });
}
