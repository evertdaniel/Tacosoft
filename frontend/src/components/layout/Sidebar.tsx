import { RoleNav } from './RoleNav';
import { Role } from '@/utils/roles';

interface SidebarProps {
  role: Role;
}

export function Sidebar({ role }: SidebarProps) {
  return (
    <aside
      className="sticky top-0 hidden h-screen w-64 flex-col border-r border-neutral-200 bg-white p-4 lg:flex"
      aria-label="Sidebar navigation"
    >
      <div className="mb-6 text-xl font-bold text-blue-700">Tacosoft</div>
      <RoleNav role={role} />
    </aside>
  );
}
