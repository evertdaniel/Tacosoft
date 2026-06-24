import { describe, it, expect } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import { StaffPlanningReportView } from './StaffPlanningReportView';
import { staffPlanningReportFixture } from '@/test/fixtures';

describe('StaffPlanningReportView', () => {
  it('renders staff planning heading and date', () => {
    render(<StaffPlanningReportView report={staffPlanningReportFixture} />);

    expect(screen.getByRole('heading', { name: /staff planning/i })).toBeInTheDocument();
    expect(screen.getByText(`Date: ${staffPlanningReportFixture.date}`)).toBeInTheDocument();
  });

  it('renders hourly workload rows', () => {
    render(<StaffPlanningReportView report={staffPlanningReportFixture} />);

    const rows = screen.getAllByRole('row');
    expect(rows.length).toBeGreaterThan(1);

    expect(screen.getByText('11:00')).toBeInTheDocument();
    expect(
      screen.getByText(staffPlanningReportFixture.hourlyWorkload[0].workloadLevel)
    ).toBeInTheDocument();
  });

  it('renders staff recommendation summary', () => {
    render(<StaffPlanningReportView report={staffPlanningReportFixture} />);

    const recommendationSection = screen.getByRole('heading', { name: /recommendation/i })
      .parentElement as HTMLElement;

    expect(recommendationSection).toBeInTheDocument();
    expect(
      within(recommendationSection).getByText(
        String(staffPlanningReportFixture.staffRecommendation.recommendedStaff)
      )
    ).toBeInTheDocument();
    expect(
      within(recommendationSection).getByText(
        staffPlanningReportFixture.staffRecommendation.rationale
      )
    ).toBeInTheDocument();
  });

  it('renders empty state when there is no workload data', () => {
    render(<StaffPlanningReportView report={{ ...staffPlanningReportFixture, hourlyWorkload: [] }} />);

    expect(screen.getByText(/no workload data/i)).toBeInTheDocument();
  });
});
