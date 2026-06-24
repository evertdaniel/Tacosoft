import { FootfallReportDto } from '@/types/domain.types';
import { formatNumber } from '@/utils/formatters';

interface FootfallReportViewProps {
  report: FootfallReportDto;
}

export function FootfallReportView({ report }: FootfallReportViewProps) {
  return (
    <div className="space-y-6" data-testid="footfall-report-view">
      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h3 className="text-lg font-semibold text-neutral-900">Footfall report</h3>
        <p className="mt-1 text-sm text-neutral-500">{`Date: ${report.orderDate}`}</p>

        {report.hourlyTraffic.length === 0 ? (
          <p className="mt-4 text-sm text-neutral-500">No hourly traffic data for this date.</p>
        ) : (
          <div className="mt-4 overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-neutral-200">
                <tr>
                  <th className="py-2 font-medium text-neutral-700">Hour</th>
                  <th className="py-2 font-medium text-neutral-700">Orders</th>
                  <th className="py-2 font-medium text-neutral-700">People</th>
                  <th className="py-2 font-medium text-neutral-700">Avg people/order</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-100">
                {report.hourlyTraffic.map((row) => (
                  <tr key={row.hour}>
                    <td className="py-2 text-neutral-900">{`${String(row.hour).padStart(2, '0')}:00`}</td>
                    <td className="py-2 text-neutral-700">{row.orderCount}</td>
                    <td className="py-2 text-neutral-700">{row.totalPeople}</td>
                    <td className="py-2 text-neutral-700">{formatNumber(row.averagePeoplePerOrder)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h4 className="text-base font-semibold text-neutral-900">Peak hours</h4>
        <div className="mt-4 grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Total orders</p>
            <p className="text-lg font-semibold text-neutral-900">{report.peakHours.totalOrders}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Total people</p>
            <p className="text-lg font-semibold text-neutral-900">{report.peakHours.totalPeople}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Avg orders/hour</p>
            <p className="text-lg font-semibold text-neutral-900">
              {formatNumber(report.peakHours.averageOrdersPerHour)}
            </p>
          </div>
        </div>

        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Peak order hours</p>
            <p className="text-lg font-semibold text-neutral-900">
              {report.peakHours.peakOrderHours.length > 0
                ? report.peakHours.peakOrderHours.map((h) => `${String(h).padStart(2, '0')}:00`).join(', ')
                : '-'}
            </p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Peak people hours</p>
            <p className="text-lg font-semibold text-neutral-900">
              {report.peakHours.peakPeopleHours.length > 0
                ? report.peakHours.peakPeopleHours.map((h) => `${String(h).padStart(2, '0')}:00`).join(', ')
                : '-'}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
