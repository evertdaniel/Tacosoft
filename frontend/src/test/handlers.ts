import { http, HttpResponse } from 'msw';
import { LoginResponse } from '@/types/domain.types';

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
];
