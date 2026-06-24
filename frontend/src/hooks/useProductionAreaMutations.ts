import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  createProductionArea,
  updateProductionArea,
  deleteProductionArea,
} from '@/api/menu.api';
import type {
  ProductionAreaDto,
  CreateProductionAreaBody,
  UpdateProductionAreaBody,
} from '@/types/domain.types';

export function useCreateProductionArea() {
  const queryClient = useQueryClient();

  return useMutation<ProductionAreaDto, Error, CreateProductionAreaBody>({
    mutationFn: createProductionArea,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'productionAreas'] });
    },
  });
}

interface UpdateProductionAreaVariables {
  id: string;
  body: UpdateProductionAreaBody;
}

export function useUpdateProductionArea() {
  const queryClient = useQueryClient();

  return useMutation<ProductionAreaDto, Error, UpdateProductionAreaVariables>({
    mutationFn: ({ id, body }) => updateProductionArea(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'productionAreas'] });
    },
  });
}

export function useDeleteProductionArea() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: deleteProductionArea,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'productionAreas'] });
    },
  });
}
