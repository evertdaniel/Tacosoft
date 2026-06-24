import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  createSection,
  updateSection,
  deleteSection,
} from '@/api/menu.api';
import type {
  SectionDto,
  CreateSectionBody,
  UpdateSectionBody,
} from '@/types/domain.types';

export function useCreateSection() {
  const queryClient = useQueryClient();

  return useMutation<SectionDto, Error, CreateSectionBody>({
    mutationFn: createSection,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'sections'] });
    },
  });
}

interface UpdateSectionVariables {
  id: string;
  body: UpdateSectionBody;
}

export function useUpdateSection() {
  const queryClient = useQueryClient();

  return useMutation<SectionDto, Error, UpdateSectionVariables>({
    mutationFn: ({ id, body }) => updateSection(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'sections'] });
    },
  });
}

export function useDeleteSection() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: deleteSection,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menu', 'sections'] });
    },
  });
}
