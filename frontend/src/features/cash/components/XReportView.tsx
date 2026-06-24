import { XReportDto } from '@/types/domain.types';
import { formatCurrency, formatNumber } from '@/utils/formatters';

interface XReportViewProps {
  report: XReportDto;
}

export function XReportView({ report }: XReportViewProps) {
  return (
    <div className="space-y-4 rounded-xl border border-neutral-200 bg-white p-5 shadow-sm" data-testid="x-report-view">
      <h3 className="text-lg font-semibold text-neutral-900">X report</h3>
      <p className="text-sm text-neutral-500">Register: {report.cashRegisterId}</p>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Current balance</p>
          <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.currentBalance)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Opening amount</p>
          <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.openingAmount)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Total income</p>
          <p className="text-lg font-semibold text-green-700">{formatCurrency(report.totalIncome)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Total expenses</p>
          <p className="text-lg font-semibold text-red-700">{formatCurrency(report.totalExpenses)}</p>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Transactions</p>
          <p className="text-lg font-semibold text-neutral-900">{formatNumber(report.transactionCount)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Incomes</p>
          <p className="text-lg font-semibold text-neutral-900">{formatNumber(report.incomeCount)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Expenses</p>
          <p className="text-lg font-semibold text-neutral-900">{formatNumber(report.expenseCount)}</p>
        </div>
      </div>
    </div>
  );
}
