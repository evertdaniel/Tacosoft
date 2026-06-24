import { Outlet } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth.store';
import { useTenantStore } from '@/stores/tenant.store';
import { MobileNav } from './MobileNav';
import { Sidebar } from './Sidebar';
import { TopBar } from './TopBar';

export function Shell() {
  const user = useAuthStore((state) => state.user);
  const currentRole = useTenantStore((state) => state.currentRole);

  const role = currentRole?.name ?? user?.primaryRole?.name ?? 'ADMIN';

  return (
    <div className="flex min-h-screen flex-col bg-neutral-50">
      <TopBar />
      <div className="flex flex-1">
        <Sidebar role={role} />
        <main className="flex-1 overflow-auto p-4 pb-24 lg:p-6 lg:pb-6">
          <Outlet />
        </main>
      </div>
      <MobileNav role={role} />
    </div>
  );
}
