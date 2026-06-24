import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  useSuppliers,
  useSupplier,
  useCreateSupplier,
  useUpdateSupplier,
  useDeactivateSupplier,
  useActivateSupplier,
  useSearchSuppliers,
} from './useSuppliers';
import {
  suppliersFixture,
  createSupplierBodyFixture,
  updateSupplierBodyFixture,
} from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  });

  return function Wrapper({ children }: { children: React.ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe('useSuppliers', () => {
  it('returns the list of suppliers', async () => {
    const { result } = renderHook(() => useSuppliers(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(suppliersFixture);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/suppliers', () => {
        return new HttpResponse(JSON.stringify({ message: 'Server error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useSuppliers(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Server error');
  });
});

describe('useSupplier', () => {
  it('returns a single supplier', async () => {
    const { result } = renderHook(() => useSupplier('supplier-1'), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.id).toBe('supplier-1');
    expect(result.current.data?.name).toBe(suppliersFixture[0].name);
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/suppliers/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Not found' }), { status: 404 });
      })
    );

    const { result } = renderHook(() => useSupplier('supplier-1'), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Not found');
  });
});

describe('useSearchSuppliers', () => {
  it('returns suppliers matching the query', async () => {
    const { result } = renderHook(() => useSearchSuppliers('Tortillas'), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toHaveLength(1);
    expect(result.current.data?.[0].name).toBe('Tortillas Del Norte');
  });

  it('returns an error when the request fails', async () => {
    server.use(
      http.get('http://localhost:8080/suppliers/search', () => {
        return new HttpResponse(JSON.stringify({ message: 'Search error' }), { status: 500 });
      })
    );

    const { result } = renderHook(() => useSearchSuppliers('query'), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Search error');
  });
});

describe('useCreateSupplier', () => {
  it('creates a supplier and invalidates suppliers query key', async () => {
    const { result } = renderHook(() => useCreateSupplier(), { wrapper: createWrapper() });

    result.current.mutate(createSupplierBodyFixture);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.name).toBe(createSupplierBodyFixture.name);
  });

  it('returns an error when creation fails', async () => {
    server.use(
      http.post('http://localhost:8080/suppliers', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create failed' }), { status: 400 });
      })
    );

    const { result } = renderHook(() => useCreateSupplier(), { wrapper: createWrapper() });

    result.current.mutate(createSupplierBodyFixture);

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Create failed');
  });
});

describe('useUpdateSupplier', () => {
  it('updates a supplier and returns the updated supplier', async () => {
    const { result } = renderHook(() => useUpdateSupplier(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'supplier-1', body: updateSupplierBodyFixture });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.id).toBe('supplier-1');
    expect(result.current.data?.name).toBe(updateSupplierBodyFixture.name);
  });

  it('returns an error when update fails', async () => {
    server.use(
      http.put('http://localhost:8080/suppliers/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Update failed' }), { status: 400 });
      })
    );

    const { result } = renderHook(() => useUpdateSupplier(), { wrapper: createWrapper() });

    result.current.mutate({ id: 'supplier-1', body: updateSupplierBodyFixture });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Update failed');
  });
});

describe('useDeactivateSupplier', () => {
  it('deactivates a supplier', async () => {
    const { result } = renderHook(() => useDeactivateSupplier(), { wrapper: createWrapper() });

    result.current.mutate('supplier-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.isActive).toBe(false);
  });

  it('returns an error when deactivation fails', async () => {
    server.use(
      http.put('http://localhost:8080/suppliers/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Deactivate failed' }), { status: 400 });
      })
    );

    const { result } = renderHook(() => useDeactivateSupplier(), { wrapper: createWrapper() });

    result.current.mutate('supplier-1');

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Deactivate failed');
  });
});

describe('useActivateSupplier', () => {
  it('activates a supplier', async () => {
    const { result } = renderHook(() => useActivateSupplier(), { wrapper: createWrapper() });

    result.current.mutate('supplier-2');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.isActive).toBe(true);
  });

  it('returns an error when activation fails', async () => {
    server.use(
      http.put('http://localhost:8080/suppliers/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Activate failed' }), { status: 400 });
      })
    );

    const { result } = renderHook(() => useActivateSupplier(), { wrapper: createWrapper() });

    result.current.mutate('supplier-2');

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error?.message).toBe('Activate failed');
  });
});
