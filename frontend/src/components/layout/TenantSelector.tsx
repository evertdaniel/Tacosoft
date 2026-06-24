import { useTenantStore } from '@/stores/tenant.store';

export function TenantSelector() {
  const { currentRestaurantId, availableRoles, switchRestaurant } = useTenantStore();

  if (availableRoles.length <= 1) {
    const current = availableRoles[0];
    return (
      <span className="text-sm font-medium text-neutral-700">
        {current?.restaurantName}
      </span>
    );
  }

  return (
    <select
      aria-label="Select restaurant"
      value={currentRestaurantId ?? ''}
      onChange={(event) => switchRestaurant(event.target.value)}
      className="rounded-md border border-neutral-300 bg-white px-2 py-1 text-sm text-neutral-700 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
    >
      {availableRoles.map((role) => (
        <option key={role.restaurantId} value={role.restaurantId}>
          {role.restaurantName}
        </option>
      ))}
    </select>
  );
}
