import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ReportDateFilter } from './ReportDateFilter';

describe('ReportDateFilter', () => {
  it('renders start and end date inputs with default values', () => {
    const onChange = vi.fn();
    render(
      <ReportDateFilter
        startDate="2024-01-01"
        endDate="2024-01-31"
        onChange={onChange}
      />
    );

    expect(screen.getByLabelText(/start date/i)).toHaveValue('2024-01-01');
    expect(screen.getByLabelText(/end date/i)).toHaveValue('2024-01-31');
  });

  it('calls onChange when start date changes', () => {
    const onChange = vi.fn();
    render(
      <ReportDateFilter
        startDate="2024-01-01"
        endDate="2024-01-31"
        onChange={onChange}
      />
    );

    fireEvent.change(screen.getByLabelText(/start date/i), { target: { value: '2024-02-01' } });

    expect(onChange).toHaveBeenCalledWith({ startDate: '2024-02-01', endDate: '2024-01-31' });
  });

  it('calls onChange when end date changes', () => {
    const onChange = vi.fn();
    render(
      <ReportDateFilter
        startDate="2024-01-01"
        endDate="2024-01-31"
        onChange={onChange}
      />
    );

    fireEvent.change(screen.getByLabelText(/end date/i), { target: { value: '2024-02-28' } });

    expect(onChange).toHaveBeenCalledWith({ startDate: '2024-01-01', endDate: '2024-02-28' });
  });
});
