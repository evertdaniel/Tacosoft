import { useState } from 'react';
import { useSalesReport, useProductReport, useFinancialReport, useFootfallReport, useStaffPlanningReport } from '@/hooks/useReports';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import { ReportDateFilter } from '../components/ReportDateFilter';
import { SalesReportView } from '../components/SalesReportView';
import { ProductReportView } from '../components/ProductReportView';
import { FinancialReportView } from '../components/FinancialReportView';
import { FootfallReportView } from '../components/FootfallReportView';
import { StaffPlanningReportView } from '../components/StaffPlanningReportView';
import type { DateRange } from '../components/ReportDateFilter';

type ReportTab = 'sales' | 'products' | 'finances' | 'footfall' | 'staff';

function getDefaultDateRange(): DateRange {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 30);

  return {
    startDate: start.toISOString().split('T')[0],
    endDate: end.toISOString().split('T')[0],
  };
}

export function ReportsPage() {
  const [range, setRange] = useState<DateRange>(getDefaultDateRange());
  const [activeTab, setActiveTab] = useState<ReportTab>('sales');

  const salesQuery = useSalesReport(range);
  const productQuery = useProductReport(range);
  const financialQuery = useFinancialReport(range);
  const footfallQuery = useFootfallReport(range);
  const staffQuery = useStaffPlanningReport(range);

  const activeQuery =
    activeTab === 'sales'
      ? salesQuery
      : activeTab === 'products'
        ? productQuery
        : activeTab === 'finances'
          ? financialQuery
          : activeTab === 'footfall'
            ? footfallQuery
            : staffQuery;

  function renderActiveReport() {
    if (activeQuery.isLoading) {
      return <Loading message="Loading report…" />;
    }

    if (activeQuery.isError) {
      return (
        <ErrorState
          title="Report unavailable"
          message={activeQuery.error?.message ?? 'Failed to load report.'}
          onRetry={() => activeQuery.refetch()}
        />
      );
    }

    return (
      <>
        {activeTab === 'sales' && salesQuery.data && <SalesReportView report={salesQuery.data} />}
        {activeTab === 'products' && productQuery.data && (
          <ProductReportView reports={productQuery.data} />
        )}
        {activeTab === 'finances' && financialQuery.data && (
          <FinancialReportView report={financialQuery.data} />
        )}
        {activeTab === 'footfall' && footfallQuery.data && (
          <FootfallReportView report={footfallQuery.data} />
        )}
        {activeTab === 'staff' && staffQuery.data && (
          <StaffPlanningReportView report={staffQuery.data} />
        )}
      </>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Reports</h1>

      <ReportDateFilter startDate={range.startDate} endDate={range.endDate} onChange={setRange} />

      <div className="flex gap-2 border-b border-neutral-200 pb-1">
        {(['sales', 'products', 'finances', 'footfall', 'staff'] as ReportTab[]).map((tab) => (
          <Button
            key={tab}
            variant={activeTab === tab ? 'primary' : 'secondary'}
            onClick={() => setActiveTab(tab)}
            data-testid={`tab-${tab}`}
          >
            {tab === 'sales' && 'Sales'}
            {tab === 'products' && 'Products'}
            {tab === 'finances' && 'Finances'}
            {tab === 'footfall' && 'Footfall'}
            {tab === 'staff' && 'Staff'}
          </Button>
        ))}
      </div>

      {renderActiveReport()}
    </div>
  );
}
