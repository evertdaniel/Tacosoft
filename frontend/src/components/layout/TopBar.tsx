import { Avatar } from '@/components/ui/Avatar';
import { useAuthStore } from '@/stores/auth.store';
import { useTenantStore } from '@/stores/tenant.store';
import { TenantSelector } from './TenantSelector';

export function TopBar() {
  const logout = useAuthStore((state) => state.logout);
  const user = useAuthStore((state) => state.user);
  const currentRestaurant = useAuthStore((state) => state.currentRestaurant);
  const currentRole = useTenantStore((state) => state.currentRole);

  const displayName = user ? `${user.firstName} ${user.lastName}` : 'User';

  return (
    <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-neutral-200 bg-white px-4 shadow-sm">
      <div className="flex items-center gap-4">
        <div className="text-lg font-bold text-blue-700 lg:hidden">Tacosoft</div>
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-neutral-700">{currentRestaurant?.name}</span>
          <TenantSelector />
          {currentRole && (
            <span className="rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-700">
              {currentRole.name}
            </span>
          )}
        </div>
      </div>

      <div className="flex items-center gap-3">
        <div className="text-right">
          <div className="text-sm font-medium text-neutral-900">{displayName}</div>
          <div className="text-xs text-neutral-500 lg:hidden">
            {currentRestaurant?.name} · {currentRole?.name}
          </div>
        </div>
        <Avatar name={displayName} />
        <button
          type="button"
          onClick={logout}
          className="rounded-md px-3 py-1.5 text-sm font-medium text-red-600 hover:bg-red-50"
        >
          Logout
        </button>
      </div>
    </header>
  );
}
