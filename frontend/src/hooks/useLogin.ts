import { useMutation } from '@tanstack/react-query';
import { login } from '@/api/auth.api';
import { useAuthStore } from '@/stores/auth.store';
import { useTenantStore } from '@/stores/tenant.store';

export function useLogin() {
  const setAuth = useAuthStore((state) => state.setAuth);
  const setTenant = useTenantStore((state) => state.setTenant);

  return useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      setAuth(data);
      setTenant(data.user.restaurantRoles, data.currentRestaurant);
    },
  });
}
