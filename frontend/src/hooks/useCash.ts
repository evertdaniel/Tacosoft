import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getCashRegisters,
  getActiveCashRegister,
  openCashRegister,
  closeCashRegister,
  getXReport,
  getZReport,
} from '@/api/cash.api';
import type { OpenCashRegisterBody, CloseCashRegisterBody } from '@/types/domain.types';

export function useCashRegisters() {
  return useQuery({
    queryKey: ['cash-registers'],
    queryFn: getCashRegisters,
  });
}

export function useActiveCashRegister() {
  return useQuery({
    queryKey: ['cash-registers', 'active'],
    queryFn: getActiveCashRegister,
  });
}

export function useOpenCashRegister() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: OpenCashRegisterBody) => openCashRegister(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cash-registers'] });
    },
  });
}

export function useCloseCashRegister() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: CloseCashRegisterBody }) => closeCashRegister(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cash-registers'] });
    },
  });
}

export function useXReport(options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: ['cash-registers', 'x-report'],
    queryFn: getXReport,
    enabled: options.enabled,
  });
}

export function useZReport() {
  return useQuery({
    queryKey: ['cash-registers', 'z-report'],
    queryFn: getZReport,
  });
}
