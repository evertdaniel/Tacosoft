import { describe, it, expect } from 'vitest';
import {
  getCashRegisters,
  getActiveCashRegister,
  openCashRegister,
  closeCashRegister,
  getXReport,
  getZReport,
} from './cash.api';
import {
  cashRegistersFixture,
  activeCashRegisterFixture,
  openCashRegisterBodyFixture,
  closeCashRegisterBodyFixture,
  xReportFixture,
  zReportFixture,
} from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

describe('cash API', () => {
  it('getCashRegisters returns the list of cash registers', async () => {
    const registers = await getCashRegisters();

    expect(registers).toEqual(cashRegistersFixture);
  });

  it('getActiveCashRegister returns the active register', async () => {
    const register = await getActiveCashRegister();

    expect(register).toEqual(activeCashRegisterFixture);
    expect(register.status).toBe('OPEN');
  });

  it('openCashRegister sends the body and returns the new register', async () => {
    const register = await openCashRegister(openCashRegisterBodyFixture);

    expect(register.openingAmount).toBe(openCashRegisterBodyFixture.openingAmount);
    expect(register.status).toBe('OPEN');
  });

  it('closeCashRegister sends the body and returns the Z report', async () => {
    const report = await closeCashRegister('cash-1', closeCashRegisterBodyFixture);

    expect(report.cashRegisterId).toBe('cash-1');
    expect(report.declaredAmount).toBe(closeCashRegisterBodyFixture.closingAmount);
  });

  it('getXReport returns the current X report', async () => {
    const report = await getXReport();

    expect(report).toEqual(xReportFixture);
  });

  it('getZReport returns the latest Z report', async () => {
    const report = await getZReport();

    expect(report).toEqual(zReportFixture);
  });

  it('openCashRegister throws when the request fails', async () => {
    server.use(
      http.post('http://localhost:8080/cash-registers/open', () => {
        return new HttpResponse(JSON.stringify({ message: 'Already open' }), { status: 409 });
      })
    );

    await expect(openCashRegister(openCashRegisterBodyFixture)).rejects.toThrow('Already open');
  });

  it('getXReport throws when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/cash-registers/x-report', () => {
        return new HttpResponse(JSON.stringify({ message: 'No active register' }), { status: 404 });
      })
    );

    await expect(getXReport()).rejects.toThrow('No active register');
  });
});
