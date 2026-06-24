import { DashboardReportDto, TableDto, TableStatus } from '@/types/domain.types';

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
