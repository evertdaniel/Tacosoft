import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/stores/auth.store';
import { useTenantStore } from '@/stores/tenant.store';
import { redirectToLogin } from '@/utils/navigation';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().token;
  const currentRestaurantId = useTenantStore.getState().currentRestaurantId;

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  if (token && currentRestaurantId) {
    config.headers['x-restaurant-id'] = currentRestaurantId;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      useAuthStore.getState().logout();
      redirectToLogin();
    }
    return Promise.reject(error);
  }
);

export default api;
