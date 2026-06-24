import { NavLink } from 'react-router-dom';
import { getNavItems, Role } from '@/utils/roles';

interface MobileNavProps {
  role: Role;
}

export function MobileNav({ role }: MobileNavProps) {
  const items = getNavItems(role);

  return (
    <nav
      aria-label="Mobile"
      className="fixed bottom-0 left-0 right-0 flex border-t border-neutral-200 bg-white px-2 pb-safe lg:hidden"
    >
      {items.map((item) => (
        <NavLink
          key={item.path}
          to={item.path}
          className={({ isActive }) =>
            `flex flex-1 flex-col items-center justify-center py-2 text-xs font-medium ${
              isActive ? 'text-blue-700' : 'text-neutral-600'
            }`
          }
        >
          <span className="h-2 w-2 rounded-full bg-current"></span>
          <span className="mt-1">{item.label}</span>
        </NavLink>
      ))}
    </nav>
  );
}
