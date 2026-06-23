import { describe, it, expect, beforeEach } from 'vitest';
import { redirectToLogin } from './navigation';

describe('redirectToLogin', () => {
  let hrefValue: string;

  beforeEach(() => {
    hrefValue = 'http://localhost:3000/';
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: {
        get href() {
          return hrefValue;
        },
        set href(value: string) {
          hrefValue = value;
        },
      },
    });
  });

  it('navigates to /login', () => {
    redirectToLogin();
    expect(window.location.href).toBe('/login');
  });
});
