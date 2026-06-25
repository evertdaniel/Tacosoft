import { create } from 'zustand';
import { LoginResponse, RestaurantInfoDto, UserDto } from '@/types/domain.types';
import { decodeExp } from '@/utils/jwt';
import { getItem, removeItem, setItem } from '@/utils/storage';
import { resetTenantStore } from '@/stores/tenant.store';

export interface AuthState {
  token: string | null;
  user: UserDto | null;
  currentRestaurant: RestaurantInfoDto | null;
  expiresAt: number | null;
  isAuthenticated: boolean;
  setAuth: (response: LoginResponse) => void;
  logout: () => void;
  isTokenExpired: () => boolean;
}

function loadInitialState(): Pick<AuthState, 'token' | 'user' | 'currentRestaurant' | 'expiresAt' | 'isAuthenticated'> {
  const token = getItem<string>('token');
  const user = getItem<UserDto>('user');
  const currentRestaurant = getItem<RestaurantInfoDto>('currentRestaurant');
  const exp = token ? decodeExp(token) : null;

  return {
    token,
    user,
    currentRestaurant,
    expiresAt: exp,
    isAuthenticated: Boolean(token && user),
  };
}

export const useAuthStore = create<AuthState>((set, get) => ({
  ...loadInitialState(),

  setAuth: (response) => {
    const exp = decodeExp(response.token);

    setItem('token', response.token);
    setItem('user', response.user);
    setItem('currentRestaurant', response.currentRestaurant);

    set({
      token: response.token,
      user: response.user,
      currentRestaurant: response.currentRestaurant,
      expiresAt: exp,
      isAuthenticated: true,
    });
  },

  logout: () => {
    removeItem('token');
    removeItem('user');
    removeItem('currentRestaurant');
    removeItem('restaurantRoles');

    // Reset in-memory tenant state immediately so the Axios interceptor cannot
    // read a stale currentRestaurantId between the logout call and the page redirect.
    resetTenantStore();

    set({
      token: null,
      user: null,
      currentRestaurant: null,
      expiresAt: null,
      isAuthenticated: false,
    });
  },

  isTokenExpired: () => {
    const { expiresAt } = get();
    if (!expiresAt) return true;
    return Date.now() >= expiresAt * 1000;
  },
}));

export function resetAuthStore(): void {
  useAuthStore.setState({
    token: null,
    user: null,
    currentRestaurant: null,
    expiresAt: null,
    isAuthenticated: false,
  });
}
