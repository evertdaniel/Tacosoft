import { createBrowserRouter, Navigate, RouteObject } from 'react-router-dom';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage';
import { TablesPage } from '@/features/tables/pages/TablesPage';
import { MenuPage } from '@/features/menu/pages/MenuPage';
import { OrdersPage } from '@/features/orders/pages/OrdersPage';
import { OrderDetailPage } from '@/features/orders/pages/OrderDetailPage';
import { KitchenPage } from '@/features/orders/pages/KitchenPage';
import { InvoicesPage } from '@/features/billing/pages/InvoicesPage';
import { CashRegisterPage } from '@/features/cash/pages/CashRegisterPage';
import { ReportsPage } from '@/features/reports/pages/ReportsPage';
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
      { path: 'orders', element: <OrdersPage /> },
      { path: 'orders/:id', element: <OrderDetailPage /> },
      { path: 'kitchen', element: <KitchenPage /> },
      { path: 'billing', element: <InvoicesPage /> },
      { path: 'cash', element: <CashRegisterPage /> },
      { path: 'reports', element: <ReportsPage /> },
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
    v7_relativeSplatPath: true,
  },
});
