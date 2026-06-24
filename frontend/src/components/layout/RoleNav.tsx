import { NavLink } from 'react-router-dom';
import { getNavItems, Role } from '@/utils/roles';

interface RoleNavProps {
  role: Role;
  onNavigate?: () => void;
}

export function RoleNav({ role, onNavigate }: RoleNavProps) {
  const items = getNavItems(role);

  return (
    <nav aria-label="Main" className="flex flex-col gap-1">
      {items.map((item) => (
        <NavLink
          key={item.path}
          to={item.path}
          onClick={onNavigate}
          className={({ isActive }) =>
            `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
              isActive
                ? 'bg-blue-50 text-blue-700'
                : 'text-neutral-600 hover:bg-neutral-100 hover:text-neutral-900'
            }`
          }
        >
          {item.label}
        </NavLink>
      ))}
    </nav>
  );
}
