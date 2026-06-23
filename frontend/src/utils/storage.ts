const STORAGE_PREFIX = 'tacosoft:v1:';

export function getItem<T>(key: string): T | null {
  try {
    const raw = localStorage.getItem(`${STORAGE_PREFIX}${key}`);
    if (raw === null) return null;
    return JSON.parse(raw) as T;
  } catch {
    console.warn(`Failed to parse storage key "${key}"`);
    return null;
  }
}

export function setItem<T>(key: string, value: T): void {
  localStorage.setItem(`${STORAGE_PREFIX}${key}`, JSON.stringify(value));
}

export function removeItem(key: string): void {
  localStorage.removeItem(`${STORAGE_PREFIX}${key}`);
}
