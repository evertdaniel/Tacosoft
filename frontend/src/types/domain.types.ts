export type Role = 'ADMIN' | 'WAITER' | 'COOK' | 'CASHIER';

export interface RoleDto {
  id: string;
  name: Role;
}

export interface RestaurantInfoDto {
  id: string;
  name: string;
  role?: string;
}

export interface RestaurantRoleDto {
  restaurantId: string;
  restaurantName: string;
  role: RoleDto;
}

export interface UserDto {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  active: boolean;
  primaryRole: RoleDto;
  restaurantRoles: RestaurantRoleDto[];
}

export interface LoginResponse {
  token: string;
  user: UserDto;
  currentRestaurant: RestaurantInfoDto;
}

export interface DashboardReportDto {
  occupiedTables: number;
  activeOrders: number;
  closedOrdersToday: number;
  salesToday: number;
  totalTables: number;
  lowStockProducts: number;
}

export type TableStatus = 'AVAILABLE' | 'OCCUPIED' | 'RESERVED' | 'CLEANING';

export interface TableDto {
  id: string;
  num: number;
  seats: number;
  status: TableStatus;
  posX: number;
  posY: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateTableStatusBody {
  status: TableStatus;
}
