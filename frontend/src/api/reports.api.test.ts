import { describe, it, expect } from 'vitest';
import { http, HttpResponse } from 'msw';
import { getDashboardReport } from './reports.api';
import { dashboardReportFixture } from '@/test/fixtures';
import { server } from '@/test/server';

describe('reports.api', () => {
  it('returns dashboard report data', async () => {
    const response = await getDashboardReport();

    expect(response).toEqual(dashboardReportFixture);
  });

  it('throws when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/dashboard', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), {
          status: 500,
          headers: { 'Content-Type': 'application/json' },
        });
      })
    );

    await expect(getDashboardReport()).rejects.toThrow('Server error');
  });
});
