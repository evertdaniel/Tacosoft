import { ZReportDto } from '@/types/domain.types';
import { formatCurrency, formatNumber } from '@/utils/formatters';

interface ZReportViewProps {
  report: ZReportDto;
}

export function ZReportView({ report }: ZReportViewProps) {
  const isBalanced = report.status === 'BALANCED';

  return (
    <div className="space-y-4 rounded-xl border border-neutral-200 bg-white p-5 shadow-sm" data-testid="z-report-view">
      <div className="flex items-start justify-between">
        <div>
          <h3 className="text-lg font-semibold text-neutral-900">Z report</h3>
          <p className="text-sm text-neutral-500">Register: {report.cashRegisterId}</p>
        </div>
        <span
          className={`rounded-full px-2.5 py-1 text-xs font-medium ${
            isBalanced ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
          }`}
        >
          {isBalanced ? 'Balanced' : 'Difference'}
        </span>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Opening amount</p>
          <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.openingAmount)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Expected amount</p>
          <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.expectedAmount)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Declared amount</p>
          <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.declaredAmount)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Difference</p>
          <p className={`text-lg font-semibold ${report.difference === 0 ? 'text-neutral-900' : 'text-red-700'}`}>
            {formatCurrency(report.difference)}
          </p>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Total income</p>
          <p className="text-lg font-semibold text-green-700">{formatCurrency(report.totalIncome)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Total expenses</p>
          <p className="text-lg font-semibold text-red-700">{formatCurrency(report.totalExpenses)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Net</p>
          <p className="text-lg font-semibold text-neutral-900">{formatCurrency(report.totalIncome - report.totalExpenses)}</p>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Income count</p>
          <p className="text-lg font-semibold text-neutral-900">{formatNumber(report.incomeCount)}</p>
        </div>
        <div className="rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Expense count</p>
          <p className="text-lg font-semibold text-neutral-900">{formatNumber(report.expenseCount)}</p>
        </div>
      </div>
    </div>
  );
}
