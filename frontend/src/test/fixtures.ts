import {
  DashboardReportDto,
  TableDto,
  TableStatus,
  SectionDto,
  CategoryDto,
  ProductDto,
  ProductOptionDto,
  ProductionAreaDto,
  OrderDto,
  OrderDetailDto,
  OrderType,
  OrderStatus,
  OrderDetailStatus,
  InvoiceDto,
  PaymentMethod,
} from '@/types/domain.types';

export const dashboardReportFixture: DashboardReportDto = {
  occupiedTables: 8,
  activeOrders: 12,
  closedOrdersToday: 24,
  salesToday: 158750.5,
  totalTables: 15,
  lowStockProducts: 3,
};

export const tablesFixture: TableDto[] = [
  { id: 'table-1', num: 1, seats: 4, status: 'AVAILABLE', posX: 0, posY: 0, active: true, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
  { id: 'table-2', num: 2, seats: 2, status: 'OCCUPIED', posX: 1, posY: 0, active: true, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
  { id: 'table-3', num: 3, seats: 6, status: 'RESERVED', posX: 2, posY: 0, active: true, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
];

export function createTableFixture(id: string, num: number, status: TableStatus, seats = 4): TableDto {
  return {
    id,
    num,
    seats,
    status,
    posX: 0,
    posY: 0,
    active: true,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  };
}

export const sectionsFixture: SectionDto[] = [
  { id: 'section-1', restaurantId: 'rest-1', name: 'Food', description: 'Main food section', displayOrder: 1, isActive: true },
  { id: 'section-2', restaurantId: 'rest-1', name: 'Drinks', description: 'Beverage section', displayOrder: 2, isActive: true },
];

export const categoriesFixture: CategoryDto[] = [
  { id: 'category-1', name: 'Tacos', description: 'Mexican tacos', sectionId: 'section-1', isActive: true, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
  { id: 'category-2', name: 'Burritos', description: 'Wrapped burritos', sectionId: 'section-1', isActive: true, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
];

export const productsFixture: ProductDto[] = [
  {
    id: 'product-1',
    name: 'Carne Asada Taco',
    description: 'Grilled steak taco',
    price: 8500,
    categoryId: 'category-1',
    taxRate: 0.19,
    stock: 50,
    manageStock: true,
    status: 'AVAILABLE',
    imageUrl: null,
    preparationTime: 10,
    isActive: true,
    productionAreaId: 'area-1',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'product-2',
    name: 'Chicken Burrito',
    description: 'Chicken burrito with rice',
    price: 15500,
    categoryId: 'category-2',
    taxRate: 0.19,
    stock: 30,
    manageStock: true,
    status: 'AVAILABLE',
    imageUrl: null,
    preparationTime: 12,
    isActive: true,
    productionAreaId: 'area-1',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
];

export const productOptionsFixture: ProductOptionDto[] = [
  { id: 'option-1', name: 'Extra Cheese', description: 'Add cheese', priceAdjustment: 1500, productId: 'product-1', isDefault: false, isAvailable: true, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
  { id: 'option-2', name: 'Spicy Salsa', description: 'Hot salsa', priceAdjustment: 0, productId: 'product-1', isDefault: true, isAvailable: true, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
];

export const productionAreasFixture: ProductionAreaDto[] = [
  { id: 'area-1', name: 'Kitchen', description: 'Main kitchen', createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
  { id: 'area-2', name: 'Bar', description: 'Drinks bar', createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' },
];

export function createSectionFixture(id: string, name: string, displayOrder = 1): SectionDto {
  return { id, restaurantId: 'rest-1', name, description: '', displayOrder, isActive: true };
}

export function createCategoryFixture(id: string, name: string, sectionId: string): CategoryDto {
  return { id, name, description: '', sectionId, isActive: true, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z' };
}

export const orderDetailFixture: OrderDetailDto = {
  id: 'detail-1',
  orderId: 'order-1',
  productId: 'product-1',
  productName: 'Carne Asada Taco',
  quantity: 2,
  unitPrice: 8500,
  amount: 17000,
  status: 'PENDING',
  notes: null,
  productOptionId: null,
  productOptionName: null,
  priceAdjustment: 0,
  createdAt: '2024-01-01T12:00:00Z',
  updatedAt: '2024-01-01T12:00:00Z',
};

export const orderDetailReadyFixture: OrderDetailDto = {
  ...orderDetailFixture,
  id: 'detail-2',
  status: 'READY',
};

export const ordersFixture: OrderDto[] = [
  {
    id: 'order-1',
    num: 1,
    type: 'IN_PLACE',
    status: 'PENDING',
    total: 17000,
    people: 2,
    tableId: 'table-1',
    clientId: null,
    details: [orderDetailFixture],
    createdAt: '2024-01-01T12:00:00Z',
    updatedAt: '2024-01-01T12:00:00Z',
  },
  {
    id: 'order-2',
    num: 2,
    type: 'TAKE_AWAY',
    status: 'IN_PROGRESS',
    total: 15500,
    people: 1,
    tableId: null,
    clientId: null,
    details: [orderDetailReadyFixture],
    createdAt: '2024-01-01T12:30:00Z',
    updatedAt: '2024-01-01T12:30:00Z',
  },
];

export function createOrderFixture(
  id: string,
  num: number,
  type: OrderType,
  status: OrderStatus,
  details: OrderDetailDto[] = []
): OrderDto {
  return {
    id,
    num,
    type,
    status,
    total: details.reduce((sum, d) => sum + d.amount, 0),
    people: 1,
    tableId: type === 'IN_PLACE' ? 'table-1' : null,
    clientId: null,
    details,
    createdAt: '2024-01-01T12:00:00Z',
    updatedAt: '2024-01-01T12:00:00Z',
  };
}

export function createOrderDetailFixture(
  id: string,
  productId: string,
  productName: string,
  status: OrderDetailStatus,
  quantity = 1
): OrderDetailDto {
  return {
    id,
    orderId: 'order-1',
    productId,
    productName,
    quantity,
    unitPrice: 8500,
    amount: 8500 * quantity,
    status,
    notes: null,
    productOptionId: null,
    productOptionName: null,
    priceAdjustment: 0,
    createdAt: '2024-01-01T12:00:00Z',
    updatedAt: '2024-01-01T12:00:00Z',
  };
}

export const invoicesFixture: InvoiceDto[] = [
  {
    id: 'invoice-1',
    restaurantId: 'rest-1',
    orderId: 'order-1',
    folio: 1001,
    subtotal: 17000,
    tax: 3230,
    total: 20230,
    isPaid: false,
    paymentMethod: null,
    createdAt: '2024-01-01T12:00:00Z',
    updatedAt: '2024-01-01T12:00:00Z',
  },
  {
    id: 'invoice-2',
    restaurantId: 'rest-1',
    orderId: 'order-2',
    folio: 1002,
    subtotal: 15500,
    tax: 2945,
    total: 18445,
    isPaid: true,
    paymentMethod: 'CASH',
    createdAt: '2024-01-01T13:00:00Z',
    updatedAt: '2024-01-01T13:30:00Z',
  },
];

export const unpaidInvoicesFixture: InvoiceDto[] = invoicesFixture.filter((invoice) => !invoice.isPaid);

export function createInvoiceFixture(
  id: string,
  folio: number,
  isPaid: boolean,
  paymentMethod: PaymentMethod | null = null
): InvoiceDto {
  return {
    id,
    restaurantId: 'rest-1',
    orderId: 'order-1',
    folio,
    subtotal: 17000,
    tax: 3230,
    total: 20230,
    isPaid,
    paymentMethod,
    createdAt: '2024-01-01T12:00:00Z',
    updatedAt: '2024-01-01T12:00:00Z',
  };
}

export const paymentBodyFixture = {
  amount: 20230,
  paymentMethod: 'CASH' as PaymentMethod,
  referenceId: 'ref-123',
};
