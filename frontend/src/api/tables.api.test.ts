import { describe, it, expect } from 'vitest';
import { getTables, updateTableStatus } from './tables.api';
import { tablesFixture } from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

describe('tables API', () => {
  it('getTables returns the list of tables', async () => {
    const tables = await getTables();

    expect(tables).toEqual(tablesFixture);
  });

  it('updateTableStatus sends the new status and returns the updated table', async () => {
    const updated = await updateTableStatus('table-2', 'AVAILABLE');

    expect(updated.id).toBe('table-2');
    expect(updated.status).toBe('AVAILABLE');
  });

  it('updateTableStatus throws when the request fails', async () => {
    server.use(
      http.put('http://localhost:8080/tables/:id/status', () => {
        return new HttpResponse(JSON.stringify({ message: 'Invalid transition' }), { status: 409 });
      })
    );

    await expect(updateTableStatus('table-2', 'CLEANING')).rejects.toThrow('Invalid transition');
  });
});
