import { describe, it, expect } from 'vitest';
import { login } from './auth.api';
import { loginResponseFixture } from '@/test/handlers';

describe('auth.api', () => {
  it('returns LoginResponse on valid credentials', async () => {
    const response = await login({ username: 'admin', password: 'secret' });

    expect(response).toEqual(loginResponseFixture);
  });

  it('throws on invalid credentials', async () => {
    await expect(login({ username: 'admin', password: 'wrong' })).rejects.toThrow();
  });
});
