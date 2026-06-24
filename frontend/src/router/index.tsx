import { createBrowserRouter, Navigate, RouteObject } from 'react-router-dom';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { ProtectedRoute } from './guarded-routes';

export const routes: RouteObject[] = [
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <div data-testid="shell-placeholder" className="p-6">Shell placeholder</div>
      </ProtectedRoute>
    ),
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
];

export const router = createBrowserRouter(routes, {
  future: {
    v7_startTransition: true,
    v7_relativeSplatPath: true,
  },
});
