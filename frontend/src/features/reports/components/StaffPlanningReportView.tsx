import { StaffPlanningReportDto } from '@/types/domain.types';

interface StaffPlanningReportViewProps {
  report: StaffPlanningReportDto;
}

export function StaffPlanningReportView({ report }: StaffPlanningReportViewProps) {
  return (
    <div className="space-y-6" data-testid="staff-planning-report-view">
      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h3 className="text-lg font-semibold text-neutral-900">Staff planning</h3>
        <p className="mt-1 text-sm text-neutral-500">{`Date: ${report.date}`}</p>

        {report.hourlyWorkload.length === 0 ? (
          <p className="mt-4 text-sm text-neutral-500">No workload data for this date.</p>
        ) : (
          <div className="mt-4 overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-neutral-200">
                <tr>
                  <th className="py-2 font-medium text-neutral-700">Hour</th>
                  <th className="py-2 font-medium text-neutral-700">Active orders</th>
                  <th className="py-2 font-medium text-neutral-700">People</th>
                  <th className="py-2 font-medium text-neutral-700">Workload</th>
                  <th className="py-2 font-medium text-neutral-700">Recommended staff</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-100">
                {report.hourlyWorkload.map((row) => (
                  <tr key={row.hour}>
                    <td className="py-2 text-neutral-900">{`${String(row.hour).padStart(2, '0')}:00`}</td>
                    <td className="py-2 text-neutral-700">{row.activeOrders}</td>
                    <td className="py-2 text-neutral-700">{row.totalPeople}</td>
                    <td className="py-2 text-neutral-700">{row.workloadLevel}</td>
                    <td className="py-2 text-neutral-700">{row.recommendedStaff}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
        <h4 className="text-base font-semibold text-neutral-900">Recommendation</h4>
        <div className="mt-4 grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Minimum staff</p>
            <p className="text-lg font-semibold text-neutral-900">{report.staffRecommendation.minimumStaff}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Recommended staff</p>
            <p className="text-lg font-semibold text-neutral-900">{report.staffRecommendation.recommendedStaff}</p>
          </div>
          <div className="rounded-lg bg-neutral-50 p-3">
            <p className="text-sm text-neutral-500">Peak staff</p>
            <p className="text-lg font-semibold text-neutral-900">{report.staffRecommendation.peakStaff}</p>
          </div>
        </div>

        <div className="mt-4 rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Peak hours</p>
          <p className="text-base font-semibold text-neutral-900">
            {report.staffRecommendation.peakHours.length > 0
              ? report.staffRecommendation.peakHours.join(', ')
              : '-'}
          </p>
        </div>

        <div className="mt-4 rounded-lg bg-neutral-50 p-3">
          <p className="text-sm text-neutral-500">Rationale</p>
          <p className="text-base text-neutral-800">{report.staffRecommendation.rationale}</p>
        </div>
      </div>
    </div>
  );
}
