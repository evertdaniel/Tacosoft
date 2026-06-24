import { SalesSummaryDto } from '@/types/domain.types';
import { formatCurrency } from '@/utils/formatters';

interface SalesReportViewProps {
  report: SalesSummaryDto;
}

export function SalesReportView({ report }: SalesReportViewProps) {
  return (
    <div className="space-y-6" data-testid="sales-report-view">
      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h3 className="text-lg font-semibold text-neutral-900">Sales report</h3>

        <div className="mt-4 grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Total revenue</p>
            <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.totalRevenue)}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Invoices</p>
            <p className="text-lg font-semibold text-neutral-900">{report.totalInvoices}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Average ticket</p>
            <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.averageTicket)}</p>
          </div>
        </div>
      </div>

      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h4 className="text-base font-semibold text-neutral-900">Top products</h4>
        {report.topProducts.length === 0 ? (
          <p className="mt-2 text-sm text-neutral-500">No products in this period.</p>
        ) : (
          <ul className="mt-2 divide-y divide-neutral-100">
            {report.topProducts.map((product) => (
              <li key={product.productId} className="flex items-center justify-between py-2">
                <span className="text-sm text-neutral-700">{product.productName}</span>
                <span className="text-sm font-medium text-neutral-900">
                  {product.totalQuantity} sold · {formatCurrency(product.totalRevenue)}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h4 className="text-base font-semibold text-neutral-900">Period comparison</h4>
        <div className="mt-2 grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Previous revenue</p>
            <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.periodComparison.previousRevenue)}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Growth</p>
            <p className="text-lg font-semibold text-green-700">{formatCurrency(report.periodComparison.growth)}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Growth %</p>
            <p className="text-lg font-semibold text-neutral-900">
              {report.periodComparison.growthPercentage?.toFixed(2) ?? '-'}%
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
