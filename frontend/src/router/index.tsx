import { createBrowserRouter, Navigate, RouteObject } from 'react-router-dom';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage';
import { TablesPage } from '@/features/tables/pages/TablesPage';
import { MenuPage } from '@/features/menu/pages/MenuPage';
import { Shell } from '@/components/layout/Shell';
import { Placeholder } from '@/components/layout/Placeholder';
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
        <Shell />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'tables', element: <TablesPage /> },
      { path: 'menu', element: <MenuPage /> },
      { path: 'orders', element: <Placeholder label="Orders" /> },
      { path: 'billing', element: <Placeholder label="Billing" /> },
      { path: 'cash', element: <Placeholder label="Cash" /> },
      { path: 'reports', element: <Placeholder label="Reports" /> },
      { path: 'suppliers', element: <Placeholder label="Suppliers" /> },
    ],
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
