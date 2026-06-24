import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { KpiGrid } from './KpiGrid';
import { DashboardReportDto } from '@/types/domain.types';

const report: DashboardReportDto = {
  occupiedTables: 8,
  activeOrders: 12,
  closedOrdersToday: 24,
  salesToday: 158750.5,
  totalTables: 15,
  lowStockProducts: 3,
};

describe('KpiGrid', () => {
  it('renders four KPI cards with the correct labels', () => {
    render(<KpiGrid report={report} />);

    expect(screen.getByText('Occupied Tables')).toBeInTheDocument();
    expect(screen.getByText('Active Orders')).toBeInTheDocument();
    expect(screen.getByText('Sales Today')).toBeInTheDocument();
    expect(screen.getByText('Low Stock')).toBeInTheDocument();
  });

  it('formats values using the report data', () => {
    render(<KpiGrid report={report} />);

    expect(screen.getByText('8 / 15')).toBeInTheDocument();
    expect(screen.getByText('12')).toBeInTheDocument();
    expect(screen.getByText('$158,750.50')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
  });
});
