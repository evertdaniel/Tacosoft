import { type ComponentType, type SVGProps } from 'react';

interface KpiCardProps {
  label: string;
  value: string;
  icon?: ComponentType<SVGProps<SVGSVGElement>>;
}

export function KpiCard({ label, value, icon: Icon }: KpiCardProps) {
  return (
    <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm" data-testid="kpi-card">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-neutral-500">{label}</p>
          <p className="mt-1 text-2xl font-semibold text-neutral-900">{value}</p>
        </div>
        {Icon && (
          <div className="rounded-lg bg-blue-50 p-2 text-blue-600">
            <Icon className="h-5 w-5" aria-hidden="true" />
          </div>
        )}
      </div>
    </div>
  );
}
