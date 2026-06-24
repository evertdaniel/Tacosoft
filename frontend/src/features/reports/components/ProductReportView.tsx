import { ProductReportDto } from '@/types/domain.types';
import { formatCurrency } from '@/utils/formatters';

interface ProductReportViewProps {
  reports: ProductReportDto[];
}

export function ProductReportView({ reports }: ProductReportViewProps) {
  return (
    <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm" data-testid="product-report-view">
      <h3 className="text-lg font-semibold text-neutral-900">Product report</h3>

      {reports.length === 0 ? (
        <p className="mt-2 text-sm text-neutral-500">No products in this period.</p>
      ) : (
        <div className="mt-4 overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-neutral-200">
              <tr>
                <th className="py-2 font-medium text-neutral-700">Product</th>
                <th className="py-2 font-medium text-neutral-700">Sold</th>
                <th className="py-2 font-medium text-neutral-700">Revenue</th>
                <th className="py-2 font-medium text-neutral-700">Margin</th>
                <th className="py-2 font-medium text-neutral-700">Margin %</th>
                <th className="py-2 font-medium text-neutral-700">Stock</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-neutral-100">
              {reports.map((report) => (
                <tr key={report.productId}>
                  <td className="py-2 text-neutral-900">{report.productName}</td>
                  <td className="py-2 text-neutral-700">{report.totalQuantity}</td>
                  <td className="py-2 text-neutral-900">{formatCurrency(report.totalRevenue)}</td>
                  <td className="py-2 text-neutral-900">{formatCurrency(report.totalMargin)}</td>
                  <td className="py-2 text-neutral-700">
                    {report.marginPercentage?.toFixed(2) ?? '-'}%
                  </td>
                  <td className="py-2 text-neutral-700">{report.currentStock ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
