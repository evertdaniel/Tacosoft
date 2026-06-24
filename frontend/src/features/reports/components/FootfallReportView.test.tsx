import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { FootfallReportView } from './FootfallReportView';
import { footfallReportFixture } from '@/test/fixtures';

describe('FootfallReportView', () => {
  it('renders footfall report heading and date', () => {
    render(<FootfallReportView report={footfallReportFixture} />);

    expect(screen.getByRole('heading', { name: /footfall report/i })).toBeInTheDocument();
    expect(screen.getByText(`Date: ${footfallReportFixture.orderDate}`)).toBeInTheDocument();
  });

  it('renders hourly traffic rows', () => {
    render(<FootfallReportView report={footfallReportFixture} />);

    const rows = screen.getAllByRole('row');
    expect(rows.length).toBeGreaterThan(1);

    expect(screen.getByText('11:00')).toBeInTheDocument();
    expect(
      screen.getByText(String(footfallReportFixture.hourlyTraffic[0].orderCount))
    ).toBeInTheDocument();
  });

  it('renders peak hours summary', () => {
    render(<FootfallReportView report={footfallReportFixture} />);

    expect(screen.getByRole('heading', { name: /peak hours/i })).toBeInTheDocument();
    expect(screen.getByText(String(footfallReportFixture.peakHours.totalOrders))).toBeInTheDocument();
    expect(screen.getByText(String(footfallReportFixture.peakHours.totalPeople))).toBeInTheDocument();
  });

  it('renders empty state when there is no hourly traffic', () => {
    render(<FootfallReportView report={{ ...footfallReportFixture, hourlyTraffic: [] }} />);

    expect(screen.getByText(/no hourly traffic data/i)).toBeInTheDocument();
  });
});
