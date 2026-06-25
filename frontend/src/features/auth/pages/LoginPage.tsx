import { Navigate } from 'react-router-dom';
import { useLogin } from '@/hooks/useLogin';
import { useAuthStore } from '@/stores/auth.store';
import { LoginForm } from '../components/LoginForm';

export function LoginPage() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const { mutate, isPending, error } = useLogin();

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-neutral-50 px-4">
      <div className="mb-6 text-center">
        <h1 className="text-2xl font-bold text-neutral-800">Sign in to Tacosoft</h1>
        <p className="mt-1 text-sm text-neutral-600">Enter your credentials to continue.</p>
      </div>
      <LoginForm onSubmit={mutate} isPending={isPending} error={error} />
    </div>
  );
}
