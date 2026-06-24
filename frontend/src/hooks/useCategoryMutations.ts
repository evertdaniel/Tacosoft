import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  createCategory,
  updateCategory,
  deleteCategory,
} from '@/api/menu.api';
import type {
  CategoryDto,
  CreateCategoryBody,
  UpdateCategoryBody,
} from '@/types/domain.types';

export function useCreateCategory() {
  const queryClient = useQueryClient();

  return useMutation<CategoryDto, Error, CreateCategoryBody>({
    mutationFn: createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'categories'] });
    },
  });
}

interface UpdateCategoryVariables {
  id: string;
  body: UpdateCategoryBody;
}

export function useUpdateCategory() {
  const queryClient = useQueryClient();

  return useMutation<CategoryDto, Error, UpdateCategoryVariables>({
    mutationFn: ({ id, body }) => updateCategory(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'categories'] });
    },
  });
}

export function useDeleteCategory() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: deleteCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'categories'] });
    },
  });
}
