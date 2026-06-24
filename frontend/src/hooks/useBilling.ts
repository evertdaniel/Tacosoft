import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getInvoices, getUnpaidInvoices, payInvoice } from '@/api/billing.api';
import type { InvoiceDto, PaymentBody } from '@/types/domain.types';

export function useInvoices() {
  return useQuery({
    queryKey: ['invoices'],
    queryFn: getInvoices,
  });
}

export function useUnpaidInvoices() {
  return useQuery({
    queryKey: ['invoices', 'unpaid'],
    queryFn: getUnpaidInvoices,
  });
}

export function usePayInvoice() {
  const queryClient = useQueryClient();

  return useMutation<InvoiceDto, Error, { id: string; body: PaymentBody }>({
    mutationFn: ({ id, body }) => payInvoice(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
    },
  });
}
