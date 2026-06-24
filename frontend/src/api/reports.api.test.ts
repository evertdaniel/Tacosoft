import { describe, it, expect } from 'vitest';
import { getSalesReport, getProductReport, getFinancialReport } from './reports.api';
import {
  salesSummaryFixture,
  productReportsFixture,
  financialReportFixture,
  reportDateRangeFixture,
} from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

describe('reports API', () => {
  it('getSalesReport sends date range and returns sales summary', async () => {
    const report = await getSalesReport(reportDateRangeFixture);

    expect(report.totalRevenue).toBe(salesSummaryFixture.totalRevenue);
    expect(report.totalInvoices).toBe(salesSummaryFixture.totalInvoices);
    expect(report.topProducts).toEqual(salesSummaryFixture.topProducts);
  });

  it('getProductReport sends date range and returns product report list', async () => {
    const reports = await getProductReport(reportDateRangeFixture);

    expect(reports).toEqual(productReportsFixture);
    expect(reports[0].productName).toBe(productReportsFixture[0].productName);
  });

  it('getFinancialReport sends date range and returns financial report', async () => {
    const report = await getFinancialReport(reportDateRangeFixture);

    expect(report.netCashFlow).toBe(financialReportFixture.netCashFlow);
    expect(report.invoiceSummary.totalInvoices).toBe(
      financialReportFixture.invoiceSummary.totalInvoices
    );
  });

  it('getSalesReport throws when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/sales', () => {
        return new HttpResponse(JSON.stringify({ message: 'Sales report error' }), { status: 500 });
      })
    );

    await expect(getSalesReport(reportDateRangeFixture)).rejects.toThrow('Sales report error');
  });

  it('getProductReport throws when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/products', () => {
        return new HttpResponse(JSON.stringify({ message: 'Product report error' }), {
          status: 500,
        });
      })
    );

    await expect(getProductReport(reportDateRangeFixture)).rejects.toThrow('Product report error');
  });

  it('getFinancialReport throws when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/reports/finances', () => {
        return new HttpResponse(JSON.stringify({ message: 'Financial report error' }), {
          status: 500,
        });
      })
    );

    await expect(getFinancialReport(reportDateRangeFixture)).rejects.toThrow(
      'Financial report error'
    );
  });
});
