import { useEffect } from 'react';
import { useAuthStore } from '@/stores/auth.store';
import { redirectToLogin } from '@/utils/navigation';

const EXPIRY_CHECK_INTERVAL_MS = 60_000;

export function useTokenExpiry() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isTokenExpired = useAuthStore((state) => state.isTokenExpired);
  const logout = useAuthStore((state) => state.logout);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    const intervalId = setInterval(() => {
      if (isTokenExpired()) {
        logout();
        redirectToLogin();
      }
    }, EXPIRY_CHECK_INTERVAL_MS);

    return () => {
      clearInterval(intervalId);
    };
  }, [isAuthenticated, isTokenExpired, logout]);
}
