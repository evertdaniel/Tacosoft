import { FinancialReportDto, TransactionSummaryDto } from '@/types/domain.types';
import { formatCurrency } from '@/utils/formatters';

interface FinancialReportViewProps {
  report: FinancialReportDto;
}

function TransactionList({ title, items }: { title: string; items: TransactionSummaryDto[] }) {
  return (
    <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
      <h4 className="text-base font-semibold text-neutral-900">{title}</h4>
      {items.length === 0 ? (
        <p className="mt-2 text-sm text-neutral-500">No transactions in this period.</p>
      ) : (
        <ul className="mt-2 divide-y divide-neutral-100">
          {items.map((item, index) => (
            <li key={`${item.paymentMethod}-${index}`} className="flex items-center justify-between py-2">
              <span className="text-sm text-neutral-700">{item.paymentMethod}</span>
              <span className="text-sm font-medium text-neutral-900">
                {item.transactionCount} · {formatCurrency(item.totalAmount)}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function FinancialReportView({ report }: FinancialReportViewProps) {
  const { cashRegisterSummary, invoiceSummary } = report;

  return (
    <div className="space-y-6" data-testid="financial-report-view">
      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h3 className="text-lg font-semibold text-neutral-900">Financial report</h3>
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Net cash flow</p>
            <p className={`text-lg font-semibold ${report.netCashFlow >= 0 ? 'text-green-700' : 'text-red-700'}`}>
              {formatCurrency(report.netCashFlow)}
            </p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Transaction date</p>
            <p className="text-lg font-semibold text-neutral-900">{report.transactionDate}</p>
          </div>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <TransactionList title="Income" items={report.income} />
        <TransactionList title="Expenses" items={report.expenses} />
      </div>

      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h4 className="text-base font-semibold text-neutral-900">Cash register summary</h4>
        <div className="mt-4 grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Open registers</p>
            <p className="text-lg font-semibold text-neutral-900">{cashRegisterSummary.openRegisters}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Closed registers</p>
            <p className="text-lg font-semibold text-neutral-900">{cashRegisterSummary.closedRegisters}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Discrepancy</p>
            <p className="text-lg font-semibold text-neutral-900">{formatCurrency(cashRegisterSummary.discrepancy)}</p>
          </div>
        </div>
      </div>

      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h4 className="text-base font-semibold text-neutral-900">Invoice summary</h4>
        <div className="mt-4 grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Total invoices</p>
            <p className="text-lg font-semibold text-neutral-900">{invoiceSummary.totalInvoices}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Paid / unpaid</p>
            <p className="text-lg font-semibold text-neutral-900">
              {invoiceSummary.paidInvoices} / {invoiceSummary.unpaidInvoices}
            </p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Payment rate</p>
            <p className="text-lg font-semibold text-neutral-900">
              {invoiceSummary.paymentRate?.toFixed(2) ?? '-'}%
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
