import api from './axios';
import { LoginResponse } from '@/types/domain.types';
import { LoginRequestBody } from '@/types/api.types';
import { AxiosError } from 'axios';

export async function login(credentials: LoginRequestBody): Promise<LoginResponse> {
  try {
    const { data } = await api.post<LoginResponse>('/auth/login', credentials);
    return data;
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>;
    const message = axiosError.response?.data?.message ?? axiosError.message;
    throw new Error(message);
  }
}
