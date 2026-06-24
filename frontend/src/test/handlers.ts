import { http, HttpResponse } from 'msw';
import { LoginResponse, TableDto, TableStatus } from '@/types/domain.types';
import {
  dashboardReportFixture,
  tablesFixture,
  sectionsFixture,
  categoriesFixture,
  productsFixture,
  productOptionsFixture,
  productionAreasFixture,
} from './fixtures';

export const loginResponseFixture: LoginResponse = {
  token:
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLTEiLCJleHAiOjk5OTk5OTk5OTl9.mock-signature',
  user: {
    id: 'user-1',
    username: 'admin',
    firstName: 'Admin',
    lastName: 'User',
    email: 'admin@tacosoft.com',
    active: true,
    primaryRole: { id: 'role-admin', name: 'ADMIN' },
    restaurantRoles: [
      {
        restaurantId: 'rest-1',
        restaurantName: 'Taqueria Principal',
        role: { id: 'role-admin', name: 'ADMIN' },
      },
    ],
  },
  currentRestaurant: { id: 'rest-1', name: 'Taqueria Principal', role: 'ADMIN' },
};

export const handlers = [
  http.post('http://localhost:8080/auth/login', async ({ request }) => {
    const body = (await request.json()) as { username?: string; password?: string };

    if (body.username === 'admin' && body.password === 'secret') {
      return HttpResponse.json(loginResponseFixture);
    }

    return new HttpResponse(JSON.stringify({ message: 'Invalid credentials' }), {
      status: 401,
      headers: { 'Content-Type': 'application/json' },
    });
  }),
  http.get('http://localhost:8080/reports/dashboard', () => {
    return HttpResponse.json(dashboardReportFixture);
  }),
  http.get('http://localhost:8080/tables', () => {
    return HttpResponse.json(tablesFixture);
  }),
  http.put('http://localhost:8080/tables/:id/status', async ({ request, params }) => {
    const body = (await request.json()) as { status?: TableStatus };
    const table = tablesFixture.find((t) => t.id === params.id) ?? tablesFixture[0];
    const updated: TableDto = { ...table, id: params.id as string, status: body.status ?? table.status };
    return HttpResponse.json(updated);
  }),
  http.get('http://localhost:8080/sections', () => {
    return HttpResponse.json(sectionsFixture);
  }),
  http.post('http://localhost:8080/sections', async ({ request }) => {
    const body = (await request.json()) as { name: string; description?: string; displayOrder: number; isActive: boolean };
    const section = sectionsFixture[0];
    return HttpResponse.json({ ...section, ...body, id: 'section-new' }, { status: 201 });
  }),
  http.put('http://localhost:8080/sections/:id', async ({ request, params }) => {
    const body = (await request.json()) as { name?: string; description?: string; displayOrder?: number; isActive?: boolean };
    const section = sectionsFixture.find((s) => s.id === params.id) ?? sectionsFixture[0];
    return HttpResponse.json({ ...section, ...body, id: params.id as string });
  }),
  http.delete('http://localhost:8080/sections/:id', () => {
    return new HttpResponse(null, { status: 204 });
  }),
  http.get('http://localhost:8080/categories', () => {
    return HttpResponse.json(categoriesFixture);
  }),
  http.post('http://localhost:8080/categories', async ({ request }) => {
    const body = (await request.json()) as { name: string; description?: string; sectionId: string };
    const category = categoriesFixture[0];
    return HttpResponse.json({ ...category, ...body, id: 'category-new' }, { status: 201 });
  }),
  http.put('http://localhost:8080/categories/:id', async ({ request, params }) => {
    const body = (await request.json()) as { name?: string; description?: string; isActive?: boolean };
    const category = categoriesFixture.find((c) => c.id === params.id) ?? categoriesFixture[0];
    return HttpResponse.json({ ...category, ...body, id: params.id as string });
  }),
  http.delete('http://localhost:8080/categories/:id', () => {
    return new HttpResponse(null, { status: 204 });
  }),
  http.get('http://localhost:8080/products', () => {
    return HttpResponse.json(productsFixture);
  }),
  http.post('http://localhost:8080/products', async ({ request }) => {
    const body = (await request.json()) as { name: string; description?: string; price: number; categoryId: string };
    const product = productsFixture[0];
    return HttpResponse.json({ ...product, ...body, id: 'product-new' }, { status: 201 });
  }),
  http.put('http://localhost:8080/products/:id', async ({ request, params }) => {
    const body = (await request.json()) as { name?: string; description?: string; price?: number };
    const product = productsFixture.find((p) => p.id === params.id) ?? productsFixture[0];
    return HttpResponse.json({ ...product, ...body, id: params.id as string });
  }),
  http.delete('http://localhost:8080/products/:id', () => {
    return new HttpResponse(null, { status: 204 });
  }),
  http.get('http://localhost:8080/product-options', () => {
    return HttpResponse.json(productOptionsFixture);
  }),
  http.post('http://localhost:8080/product-options', async ({ request }) => {
    const body = (await request.json()) as { name: string; description?: string; priceAdjustment: number; productId: string };
    const option = productOptionsFixture[0];
    return HttpResponse.json({ ...option, ...body, id: 'option-new' }, { status: 201 });
  }),
  http.put('http://localhost:8080/product-options/:id', async ({ request, params }) => {
    const body = (await request.json()) as { name?: string; description?: string; priceAdjustment?: number };
    const option = productOptionsFixture.find((o) => o.id === params.id) ?? productOptionsFixture[0];
    return HttpResponse.json({ ...option, ...body, id: params.id as string });
  }),
  http.delete('http://localhost:8080/product-options/:id', () => {
    return new HttpResponse(null, { status: 204 });
  }),
  http.get('http://localhost:8080/production-areas', () => {
    return HttpResponse.json(productionAreasFixture);
  }),
  http.post('http://localhost:8080/production-areas', async ({ request }) => {
    const body = (await request.json()) as { name: string; description?: string };
    const area = productionAreasFixture[0];
    return HttpResponse.json({ ...area, ...body, id: 'area-new' }, { status: 201 });
  }),
  http.put('http://localhost:8080/production-areas/:id', async ({ request, params }) => {
    const body = (await request.json()) as { name?: string; description?: string };
    const area = productionAreasFixture.find((a) => a.id === params.id) ?? productionAreasFixture[0];
    return HttpResponse.json({ ...area, ...body, id: params.id as string });
  }),
  http.delete('http://localhost:8080/production-areas/:id', () => {
    return new HttpResponse(null, { status: 204 });
  }),
];
