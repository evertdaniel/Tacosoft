import { describe, expect, it } from 'vitest';
import { getNavItems, type NavItem } from './roles';

describe('getNavItems', () => {
  it('returns all management items for ADMIN', () => {
    const items = getNavItems('ADMIN');
    const paths = items.map((item: NavItem) => item.path);

    expect(paths).toContain('/dashboard');
    expect(paths).toContain('/tables');
    expect(paths).toContain('/menu');
    expect(paths).toContain('/orders');
    expect(paths).toContain('/kitchen');
    expect(paths).toContain('/billing');
    expect(paths).toContain('/cash');
    expect(paths).toContain('/reports');
    expect(paths).toContain('/suppliers');
  });

  it('returns kitchen-related items for COOK and hides billing', () => {
    const items = getNavItems('COOK');
    const paths = items.map((item: NavItem) => item.path);

    expect(paths).toContain('/dashboard');
    expect(paths).toContain('/orders');
    expect(paths).toContain('/menu');
    expect(paths).toContain('/kitchen');
    expect(paths).not.toContain('/billing');
    expect(paths).not.toContain('/cash');
    expect(paths).not.toContain('/tables');
    expect(paths).not.toContain('/reports');
    expect(paths).not.toContain('/suppliers');
  });

  it('returns tables and orders for WAITER', () => {
    const items = getNavItems('WAITER');
    const paths = items.map((item: NavItem) => item.path);

    expect(paths).toContain('/dashboard');
    expect(paths).toContain('/tables');
    expect(paths).toContain('/orders');
    expect(paths).not.toContain('/billing');
    expect(paths).not.toContain('/menu');
  });

  it('returns billing and cash for CASHIER', () => {
    const items = getNavItems('CASHIER');
    const paths = items.map((item: NavItem) => item.path);

    expect(paths).toContain('/dashboard');
    expect(paths).toContain('/billing');
    expect(paths).toContain('/cash');
    expect(paths).not.toContain('/orders');
    expect(paths).not.toContain('/tables');
  });
});
