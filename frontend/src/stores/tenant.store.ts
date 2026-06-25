import { create } from 'zustand';
import { RestaurantInfoDto, RestaurantRoleDto, RoleDto } from '@/types/domain.types';
import { getItem, setItem } from '@/utils/storage';

export interface TenantState {
  currentRestaurantId: string | null;
  currentRole: RoleDto | null;
  availableRoles: RestaurantRoleDto[];
  setTenant: (restaurantRoles: RestaurantRoleDto[], currentRestaurant: RestaurantInfoDto) => void;
  switchRestaurant: (restaurantId: string) => void;
}

function loadInitialState(): Pick<TenantState, 'currentRestaurantId' | 'currentRole' | 'availableRoles'> {
  const savedRoles = getItem<RestaurantRoleDto[]>('restaurantRoles');
  const savedRestaurant = getItem<RestaurantInfoDto>('currentRestaurant');

  const currentRestaurantId = savedRestaurant?.id ?? null;
  const currentRole =
    savedRoles && savedRestaurant
      ? (savedRoles.find((r) => r.restaurantId === savedRestaurant.id)?.role ?? null)
      : null;

  return {
    currentRestaurantId,
    currentRole,
    availableRoles: savedRoles ?? [],
  };
}

export const useTenantStore = create<TenantState>((set, get) => ({
  ...loadInitialState(),

  setTenant: (restaurantRoles, currentRestaurant) => {
    const currentRole =
      restaurantRoles.find((r) => r.restaurantId === currentRestaurant.id)?.role ?? null;

    setItem('restaurantRoles', restaurantRoles);

    set({
      currentRestaurantId: currentRestaurant.id,
      currentRole,
      availableRoles: restaurantRoles,
    });
  },

  switchRestaurant: (restaurantId) => {
    const { availableRoles } = get();
    // Security: only allow switching to a restaurant that is in the user's own roles list.
    // This prevents persisting a restaurant the authenticated user has no role for,
    // which would cause the axios interceptor to send x-restaurant-id for an unauthorized tenant.
    const match = availableRoles.find((r) => r.restaurantId === restaurantId);
    if (!match) return;

    const newCurrentRestaurant: RestaurantInfoDto = {
      id: match.restaurantId,
      name: match.restaurantName,
    };

    setItem('currentRestaurant', newCurrentRestaurant);

    set({
      currentRestaurantId: match.restaurantId,
      currentRole: match.role,
    });
  },
}));

export function resetTenantStore(): void {
  useTenantStore.setState({
    currentRestaurantId: null,
    currentRole: null,
    availableRoles: [],
  });
}
