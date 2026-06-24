import api from './axios';
import { AxiosError } from 'axios';
import type {
  CashRegisterDto,
  OpenCashRegisterBody,
  CloseCashRegisterBody,
  XReportDto,
  ZReportDto,
} from '@/types/domain.types';

function getErrorMessage(error: unknown): string {
  const axiosError = error as AxiosError<{ message?: string }>;
  return axiosError.response?.data?.message ?? axiosError.message;
}

export async function getCashRegisters(): Promise<CashRegisterDto[]> {
  try {
    const { data } = await api.get<CashRegisterDto[]>('/cash-registers');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getActiveCashRegister(): Promise<CashRegisterDto> {
  try {
    const { data } = await api.get<CashRegisterDto>('/cash-registers/active');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function openCashRegister(body: OpenCashRegisterBody): Promise<CashRegisterDto> {
  try {
    const { data } = await api.post<CashRegisterDto>('/cash-registers/open', body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function closeCashRegister(id: string, body: CloseCashRegisterBody): Promise<ZReportDto> {
  try {
    const { data } = await api.put<ZReportDto>(`/cash-registers/${id}/close`, body);
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getXReport(): Promise<XReportDto> {
  try {
    const { data } = await api.get<XReportDto>('/cash-registers/x-report');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}

export async function getZReport(): Promise<ZReportDto> {
  try {
    const { data } = await api.get<ZReportDto>('/cash-registers/z-report');
    return data;
  } catch (error) {
    throw new Error(getErrorMessage(error));
  }
}
