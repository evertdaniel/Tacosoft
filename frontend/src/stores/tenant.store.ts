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
    const match = availableRoles.find((r) => r.restaurantId === restaurantId);
    if (!match) return;

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
