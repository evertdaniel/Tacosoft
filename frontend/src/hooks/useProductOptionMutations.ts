import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  createProductOption,
  updateProductOption,
  deleteProductOption,
} from '@/api/menu.api';
import type {
  ProductOptionDto,
  CreateProductOptionBody,
  UpdateProductOptionBody,
} from '@/types/domain.types';

export function useCreateProductOption() {
  const queryClient = useQueryClient();

  return useMutation<ProductOptionDto, Error, CreateProductOptionBody>({
    mutationFn: createProductOption,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'productOptions'] });
    },
  });
}

interface UpdateProductOptionVariables {
  id: string;
  body: UpdateProductOptionBody;
}

export function useUpdateProductOption() {
  const queryClient = useQueryClient();

  return useMutation<ProductOptionDto, Error, UpdateProductOptionVariables>({
    mutationFn: ({ id, body }) => updateProductOption(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'productOptions'] });
    },
  });
}

export function useDeleteProductOption() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: deleteProductOption,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'productOptions'] });
    },
  });
}
