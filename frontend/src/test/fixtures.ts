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
  CashRegisterDto,
  OpenCashRegisterBody,
  CloseCashRegisterBody,
  XReportDto,
  ZReportDto,
  SalesSummaryDto,
  ProductReportDto,
  FinancialReportDto,
  ReportDateRangeParams,
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

export const cashRegistersFixture: CashRegisterDto[] = [
  {
    id: 'cash-1',
    restaurantId: 'rest-1',
    userId: 'user-1',
    openingAmount: 50000,
    closingAmount: null,
    status: 'OPEN',
    openedAt: '2024-01-01T08:00:00',
    closedAt: null,
  },
  {
    id: 'cash-2',
    restaurantId: 'rest-1',
    userId: 'user-1',
    openingAmount: 30000,
    closingAmount: 28000,
    status: 'CLOSED',
    openedAt: '2024-01-01T08:00:00',
    closedAt: '2024-01-01T20:00:00',
  },
];

export const activeCashRegisterFixture: CashRegisterDto = cashRegistersFixture[0];

export const openCashRegisterBodyFixture: OpenCashRegisterBody = {
  openingAmount: 50000,
};

export const closeCashRegisterBodyFixture: CloseCashRegisterBody = {
  closingAmount: 75000,
};

export const xReportFixture: XReportDto = {
  cashRegisterId: 'cash-1',
  currentBalance: 75000,
  openingAmount: 50000,
  totalIncome: 30000,
  totalExpenses: 5000,
  transactionCount: 7,
  incomeCount: 5,
  expenseCount: 2,
};

export const zReportFixture: ZReportDto = {
  cashRegisterId: 'cash-1',
  openingAmount: 50000,
  expectedAmount: 75000,
  declaredAmount: 75000,
  difference: 0,
  totalIncome: 30000,
  totalExpenses: 5000,
  incomeCount: 5,
  expenseCount: 2,
  status: 'BALANCED',
};

export const reportDateRangeFixture: ReportDateRangeParams = {
  startDate: '2024-01-01',
  endDate: '2024-01-31',
};

export const salesSummaryFixture: SalesSummaryDto = {
  totalRevenue: 500000,
  totalInvoices: 45,
  averageTicket: 11111.11,
  topProducts: [
    {
      productId: 'product-1',
      productName: 'Carne Asada Taco',
      totalQuantity: 120,
      totalRevenue: 102000,
      orderCount: 80,
    },
    {
      productId: 'product-2',
      productName: 'Chicken Burrito',
      totalQuantity: 60,
      totalRevenue: 93000,
      orderCount: 40,
    },
  ],
  periodComparison: {
    currentRevenue: 500000,
    previousRevenue: 450000,
    growth: 50000,
    growthPercentage: 11.11,
  },
};

export const productReportsFixture: ProductReportDto[] = [
  {
    productId: 'product-1',
    productName: 'Carne Asada Taco',
    orderCount: 80,
    totalQuantity: 120,
    totalRevenue: 102000,
    unitCost: 3500,
    totalMargin: 60000,
    marginPercentage: 58.82,
    currentStock: 50,
    stockTurnoverDays: 12,
  },
  {
    productId: 'product-2',
    productName: 'Chicken Burrito',
    orderCount: 40,
    totalQuantity: 60,
    totalRevenue: 93000,
    unitCost: 7000,
    totalMargin: 51000,
    marginPercentage: 54.84,
    currentStock: 30,
    stockTurnoverDays: 15,
  },
];

export const financialReportFixture: FinancialReportDto = {
  transactionDate: '2024-01-01',
  income: [
    { paymentMethod: 'CASH', transactionCount: 20, totalAmount: 150000 },
    { paymentMethod: 'CREDIT_CARD', transactionCount: 15, totalAmount: 200000 },
  ],
  expenses: [
    { paymentMethod: 'CASH', transactionCount: 5, totalAmount: 30000 },
    { paymentMethod: 'TRANSFER', transactionCount: 2, totalAmount: 20000 },
  ],
  netCashFlow: 300000,
  cashRegisterSummary: {
    openRegisters: 1,
    closedRegisters: 0,
    totalOpeningBalance: 50000,
    totalClosingBalance: 0,
    totalIncome: 350000,
    totalExpenses: 50000,
    expectedBalance: 350000,
    actualBalance: 0,
    discrepancy: -350000,
  },
  invoiceSummary: {
    totalInvoices: 45,
    paidInvoices: 35,
    unpaidInvoices: 10,
    totalInvoiced: 500000,
    totalPaid: 400000,
    totalPending: 100000,
    paymentRate: 77.78,
  },
};
