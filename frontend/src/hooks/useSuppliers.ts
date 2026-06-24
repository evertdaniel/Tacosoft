import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getSuppliers,
  getSupplier,
  createSupplier,
  updateSupplier,
  deactivateSupplier,
  activateSupplier,
  searchSuppliers,
} from '@/api/suppliers.api';
import type {
  SupplierDto,
  CreateSupplierBody,
  UpdateSupplierBody,
} from '@/types/domain.types';

function updateSupplierCache(queryClient: ReturnType<typeof useQueryClient>, data: SupplierDto) {
  queryClient.setQueryData<SupplierDto[]>(['suppliers'], (old) =>
    old?.map((supplier) => (supplier.id === data.id ? data : supplier)) ?? []
  );
  queryClient.setQueryData<SupplierDto>(['suppliers', data.id], data);
}

export function useSuppliers() {
  return useQuery({
    queryKey: ['suppliers'],
    queryFn: getSuppliers,
  });
}

export function useSupplier(id: string) {
  return useQuery({
    queryKey: ['suppliers', id],
    queryFn: () => getSupplier(id),
    enabled: Boolean(id),
  });
}

export function useSearchSuppliers(query: string) {
  return useQuery({
    queryKey: ['suppliers', 'search', query],
    queryFn: () => searchSuppliers(query),
    enabled: Boolean(query),
  });
}

export function useCreateSupplier() {
  const queryClient = useQueryClient();

  return useMutation<SupplierDto, Error, CreateSupplierBody>({
    mutationFn: createSupplier,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suppliers'] });
    },
  });
}

interface UpdateSupplierVariables {
  id: string;
  body: UpdateSupplierBody;
}

export function useUpdateSupplier() {
  const queryClient = useQueryClient();

  return useMutation<SupplierDto, Error, UpdateSupplierVariables>({
    mutationFn: ({ id, body }) => updateSupplier(id, body),
    onSuccess: (data) => {
      updateSupplierCache(queryClient, data);
    },
  });
}

export function useDeactivateSupplier() {
  const queryClient = useQueryClient();

  return useMutation<SupplierDto, Error, string>({
    mutationFn: deactivateSupplier,
    onSuccess: (data) => {
      updateSupplierCache(queryClient, data);
    },
  });
}

export function useActivateSupplier() {
  const queryClient = useQueryClient();

  return useMutation<SupplierDto, Error, string>({
    mutationFn: activateSupplier,
    onSuccess: (data) => {
      updateSupplierCache(queryClient, data);
    },
  });
}
