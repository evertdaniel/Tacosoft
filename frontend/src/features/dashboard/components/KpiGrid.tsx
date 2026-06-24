import { DashboardReportDto } from '@/types/domain.types';
import { formatCurrency, formatNumber } from '@/utils/formatters';
import { KpiCard } from './KpiCard';

interface KpiGridProps {
  report: DashboardReportDto;
}

export function KpiGrid({ report }: KpiGridProps) {
  const items = [
    { label: 'Occupied Tables', value: `${formatNumber(report.occupiedTables)} / ${formatNumber(report.totalTables)}` },
    { label: 'Active Orders', value: formatNumber(report.activeOrders) },
    { label: 'Sales Today', value: formatCurrency(report.salesToday) },
    { label: 'Low Stock', value: formatNumber(report.lowStockProducts) },
  ];

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4" data-testid="kpi-grid">
      {items.map((item) => (
        <KpiCard key={item.label} label={item.label} value={item.value} />
      ))}
    </div>
  );
}
