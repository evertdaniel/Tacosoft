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

export type ProductStatus = 'AVAILABLE' | 'OUT_OF_STOCK' | 'OUT_OF_SEASON';

export interface SectionDto {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  displayOrder: number;
  isActive: boolean;
}

export interface CategoryDto {
  id: string;
  name: string;
  description: string;
  sectionId: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductDto {
  id: string;
  name: string;
  description: string;
  price: number;
  categoryId: string;
  taxRate: number;
  stock: number;
  manageStock: boolean;
  status: ProductStatus;
  imageUrl: string | null;
  preparationTime: number;
  isActive: boolean;
  productionAreaId: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductOptionDto {
  id: string;
  name: string;
  description: string;
  priceAdjustment: number;
  productId: string;
  isDefault: boolean;
  isAvailable: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductionAreaDto {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSectionBody {
  name: string;
  description?: string;
  displayOrder: number;
  isActive: boolean;
}

export interface UpdateSectionBody {
  name?: string;
  description?: string;
  displayOrder: number;
  isActive?: boolean;
}

export interface CreateCategoryBody {
  name: string;
  description?: string;
  sectionId: string;
}

export interface UpdateCategoryBody {
  name?: string;
  description?: string;
  isActive?: boolean;
}

export interface CreateProductBody {
  name: string;
  description?: string;
  price: number;
  categoryId: string;
  taxRate?: number;
  stock?: number;
  manageStock?: boolean;
  status?: ProductStatus;
  imageUrl?: string;
  preparationTime?: number;
  isActive?: boolean;
  productionAreaId?: string;
}

export interface UpdateProductBody {
  name?: string;
  description?: string;
  price?: number;
  taxRate?: number;
  stock?: number;
  manageStock?: boolean;
  status?: ProductStatus;
  imageUrl?: string;
  preparationTime?: number;
  isActive?: boolean;
  productionAreaId?: string;
}

export interface CreateProductOptionBody {
  name: string;
  description?: string;
  priceAdjustment: number;
  productId: string;
  isDefault?: boolean;
  isAvailable?: boolean;
}

export interface UpdateProductOptionBody {
  name?: string;
  description?: string;
  priceAdjustment?: number;
  isDefault?: boolean;
  isAvailable?: boolean;
}

export interface CreateProductionAreaBody {
  name: string;
  description?: string;
}

export interface UpdateProductionAreaBody {
  name?: string;
  description?: string;
}

export type OrderType = 'IN_PLACE' | 'TAKE_AWAY';

export type OrderStatus = 'PENDING' | 'IN_PROGRESS' | 'READY' | 'DELIVERED' | 'CANCELLED' | 'CLOSED';

export type OrderDetailStatus = 'PENDING' | 'IN_PROGRESS' | 'READY' | 'DELIVERED' | 'CANCELLED';

export interface OrderDetailDto {
  id: string;
  orderId: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  amount: number;
  status: OrderDetailStatus;
  notes: string | null;
  productOptionId: string | null;
  productOptionName: string | null;
  priceAdjustment: number;
  createdAt: string;
  updatedAt: string;
}

export interface OrderDto {
  id: string;
  num: number;
  type: OrderType;
  status: OrderStatus;
  total: number;
  people: number;
  tableId: string | null;
  clientId: string | null;
  details: OrderDetailDto[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderDetailBody {
  productId: string;
  quantity: number;
  productOptionId?: string;
  notes?: string;
}

export interface CreateOrderBody {
  type: OrderType;
  people: number;
  tableId?: string;
  clientId?: string;
  details: CreateOrderDetailBody[];
}

export interface UpdateOrderDetailStatusBody {
  status: OrderDetailStatus;
}
