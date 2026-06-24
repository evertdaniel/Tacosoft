import { describe, it, expect, beforeEach, vi } from 'vitest';
import { getItem, setItem, removeItem } from './storage';

const storageKey = (key: string) => `tacosoft:v1:${key}`;

function createStorageMock() {
  const store = new Map<string, string>();
  return {
    getItem: vi.fn((key: string) => store.get(key) ?? null),
    setItem: vi.fn((key: string, value: string) => store.set(key, value)),
    removeItem: vi.fn((key: string) => store.delete(key)),
    clear: vi.fn(() => store.clear()),
  };
}

describe('storage helpers', () => {
  let storageMock: ReturnType<typeof createStorageMock>;

  beforeEach(() => {
    storageMock = createStorageMock();
    Object.defineProperty(window, 'localStorage', {
      value: storageMock,
      writable: true,
    });
  });

  it('returns parsed value when key exists', () => {
    const value = { name: 'Taco' };
    localStorage.setItem(storageKey('user'), JSON.stringify(value));

    expect(getItem<{ name: string }>('user')).toEqual(value);
  });

  it('returns null when key does not exist', () => {
    expect(getItem('missing')).toBeNull();
  });

  it('returns null and warns when stored value is invalid JSON', () => {
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    localStorage.setItem(storageKey('user'), 'not-json');

    expect(getItem('user')).toBeNull();
    expect(warnSpy).toHaveBeenCalledOnce();

    warnSpy.mockRestore();
  });

  it('serializes and stores value under prefixed key', () => {
    const value = { id: '1', name: 'Taqueria' };

    setItem('restaurant', value);

    expect(localStorage.getItem(storageKey('restaurant'))).toBe(JSON.stringify(value));
  });

  it('removes item under prefixed key', () => {
    localStorage.setItem(storageKey('token'), 'abc');

    removeItem('token');

    expect(localStorage.getItem(storageKey('token'))).toBeNull();
  });
});
