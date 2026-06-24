import api from './axios';
import { AxiosError } from 'axios';
import type { InvoiceDto, PaymentBody } from '@/types/domain.types';

function getErrorMessage(error: unknown): string {
  const axiosError = error as AxiosError<{ message?: string }>;
  return axiosError.response?.data?.message ?? axiosError.message;
}

export async function getInvoices(): Promise<InvoiceDto[]> {
  try {
    const { data } = await api.get<InvoiceDto[]>('/invoices');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getUnpaidInvoices(): Promise<InvoiceDto[]> {
  try {
    const { data } = await api.get<InvoiceDto[]>('/invoices/unpaid');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function payInvoice(id: string, body: PaymentBody): Promise<InvoiceDto> {
  try {
    const { data } = await api.post<InvoiceDto>(`/invoices/${id}/pay`, body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}
