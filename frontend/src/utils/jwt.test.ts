import { describe, it, expect } from 'vitest';
import { decodeExp } from './jwt';

function createJwtToken(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'none', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.signature`;
}

describe('decodeExp', () => {
  it('returns exp claim from a valid JWT payload', () => {
    const token = createJwtToken({ sub: 'user-1', exp: 1234567890 });

    expect(decodeExp(token)).toBe(1234567890);
  });

  it('returns null when exp claim is missing', () => {
    const token = createJwtToken({ sub: 'user-1' });

    expect(decodeExp(token)).toBeNull();
  });

  it('returns null when token format is invalid', () => {
    expect(decodeExp('not-a-jwt')).toBeNull();
    expect(decodeExp('only-one-part')).toBeNull();
  });

  it('returns null when payload is not valid JSON', () => {
    const token = `header.${btoa('not-json')}.signature`;

    expect(decodeExp(token)).toBeNull();
  });
});
