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
  { label: 'Billing', path: '/billing' },
  { label: 'Cash', path: '/cash' },
  { label: 'Reports', path: '/reports' },
  { label: 'Suppliers', path: '/suppliers' },
];

const rolePaths: Record<Role, string[]> = {
  ADMIN: allItems.map((item) => item.path),
  WAITER: ['/dashboard', '/tables', '/orders'],
  COOK: ['/dashboard', '/orders', '/menu'],
  CASHIER: ['/dashboard', '/billing', '/cash'],
};

export function getNavItems(role: Role): NavItem[] {
  const allowedPaths = rolePaths[role] ?? [];
  return allItems.filter((item) => allowedPaths.includes(item.path));
}
