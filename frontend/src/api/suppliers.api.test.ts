import { describe, it, expect } from 'vitest';
import {
  getSuppliers,
  getSupplier,
  createSupplier,
  updateSupplier,
  deactivateSupplier,
  activateSupplier,
  searchSuppliers,
} from './suppliers.api';
import {
  suppliersFixture,
  createSupplierBodyFixture,
  updateSupplierBodyFixture,
} from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

describe('suppliers API', () => {
  it('getSuppliers returns the list of suppliers', async () => {
    const suppliers = await getSuppliers();

    expect(suppliers).toEqual(suppliersFixture);
  });

  it('getSupplier returns a single supplier', async () => {
    const supplier = await getSupplier('supplier-1');

    expect(supplier.id).toBe('supplier-1');
    expect(supplier.name).toBe(suppliersFixture[0].name);
  });

  it('searchSuppliers returns matching suppliers by name', async () => {
    const suppliers = await searchSuppliers('Tortillas');

    expect(suppliers).toHaveLength(1);
    expect(suppliers[0].name).toBe('Tortillas Del Norte');
  });

  it('searchSuppliers returns an empty list when no supplier matches', async () => {
    const suppliers = await searchSuppliers('NoMatch');

    expect(suppliers).toEqual([]);
  });

  it('createSupplier sends the body and returns the created supplier', async () => {
    const supplier = await createSupplier(createSupplierBodyFixture);

    expect(supplier.id).toBe('supplier-new');
    expect(supplier.name).toBe(createSupplierBodyFixture.name);
  });

  it('updateSupplier sends the body and returns the updated supplier', async () => {
    const supplier = await updateSupplier('supplier-1', updateSupplierBodyFixture);

    expect(supplier.id).toBe('supplier-1');
    expect(supplier.name).toBe(updateSupplierBodyFixture.name);
  });

  it('deactivateSupplier sets isActive to false', async () => {
    const supplier = await deactivateSupplier('supplier-1');

    expect(supplier.id).toBe('supplier-1');
    expect(supplier.isActive).toBe(false);
  });

  it('activateSupplier sets isActive to true', async () => {
    const supplier = await activateSupplier('supplier-2');

    expect(supplier.id).toBe('supplier-2');
    expect(supplier.isActive).toBe(true);
  });

  it('getSuppliers throws when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/suppliers', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    await expect(getSuppliers()).rejects.toThrow('Server error');
  });

  it('searchSuppliers throws when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/suppliers/search', () => {
        return new HttpResponse(JSON.stringify({ message: 'Search failed' }), { status: 500 });
      })
    );

    await expect(searchSuppliers('query')).rejects.toThrow('Search failed');
  });

  it('createSupplier throws when the request fails', async () => {
    server.use(
      http.post('http://localhost:8080/suppliers', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create failed' }), { status: 400 });
      })
    );

    await expect(createSupplier(createSupplierBodyFixture)).rejects.toThrow('Create failed');
  });

  it('updateSupplier throws when the request fails', async () => {
    server.use(
      http.put('http://localhost:8080/suppliers/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Update failed' }), { status: 400 });
      })
    );

    await expect(updateSupplier('supplier-1', updateSupplierBodyFixture)).rejects.toThrow(
      'Update failed'
    );
  });

  it('deactivateSupplier throws when the request fails', async () => {
    server.use(
      http.put('http://localhost:8080/suppliers/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Deactivate failed' }), { status: 400 });
      })
    );

    await expect(deactivateSupplier('supplier-1')).rejects.toThrow('Deactivate failed');
  });

  it('activateSupplier throws when the request fails', async () => {
    server.use(
      http.put('http://localhost:8080/suppliers/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Activate failed' }), { status: 400 });
      })
    );

    await expect(activateSupplier('supplier-2')).rejects.toThrow('Activate failed');
  });
});
