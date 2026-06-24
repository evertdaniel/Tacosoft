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
  http.get('http://localhost:8080/categories', () => {
    return HttpResponse.json(categoriesFixture);
  }),
  http.get('http://localhost:8080/products', () => {
    return HttpResponse.json(productsFixture);
  }),
  http.get('http://localhost:8080/product-options', () => {
    return HttpResponse.json(productOptionsFixture);
  }),
  http.get('http://localhost:8080/production-areas', () => {
    return HttpResponse.json(productionAreasFixture);
  }),
];
