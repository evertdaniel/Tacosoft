import { Input } from '@/components/ui/Input';

export interface DateRange {
  startDate: string;
  endDate: string;
}

interface ReportDateFilterProps {
  startDate: string;
  endDate: string;
  onChange: (range: DateRange) => void;
}

export function ReportDateFilter({ startDate, endDate, onChange }: ReportDateFilterProps) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-end" data-testid="report-date-filter">
      <Input
        id="start-date"
        type="date"
        label="Start date"
        value={startDate}
        onChange={(event) => onChange({ startDate: event.target.value, endDate })}
      />
      <Input
        id="end-date"
        type="date"
        label="End date"
        value={endDate}
        onChange={(event) => onChange({ startDate, endDate: event.target.value })}
      />
    </div>
  );
}
