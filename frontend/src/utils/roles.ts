import { Role } from '@/types/domain.types';

export type { Role };

export interface NavItem {
  label: string;
  path: string;
}

const allItems: NavItem[] = [
  { label: 'Dashboard', path: '/dashboard' },
  { label: 'Tables', path: '/tables' },
  { label: 'Menu', path: '/menu' },
  { label: 'Orders', path: '/orders' },
  { label: 'Kitchen', path: '/kitchen' },
  { label: 'Billing', path: '/billing' },
  { label: 'Cash', path: '/cash' },
  { label: 'Reports', path: '/reports' },
  { label: 'Suppliers', path: '/suppliers' },
];

const rolePaths: Record<Role, string[]> = {
  ADMIN: allItems.map((item) => item.path),
  MANAGER: ['/dashboard', '/tables', '/menu', '/orders', '/billing', '/cash', '/reports', '/suppliers'],
  WAITER: ['/dashboard', '/tables', '/orders'],
  COOK: ['/dashboard', '/orders', '/menu', '/kitchen'],
  CASHIER: ['/dashboard', '/billing', '/cash'],
};

export function getNavItems(role: Role): NavItem[] {
  const allowedPaths = rolePaths[role] ?? [];
  return allItems.filter((item) => allowedPaths.includes(item.path));
}
