import { describe, it, expect } from 'vitest';
import { formatCurrency, formatNumber } from './formatters';

describe('formatters', () => {
  describe('formatCurrency', () => {
    it('formats positive amounts with two decimals', () => {
      expect(formatCurrency(1234.5)).toBe('$1,234.50');
    });

    it('formats zero as $0.00', () => {
      expect(formatCurrency(0)).toBe('$0.00');
    });

    it('formats negative amounts with the currency symbol', () => {
      expect(formatCurrency(-99.99)).toBe('-$99.99');
    });
  });

  describe('formatNumber', () => {
    it('formats integers with grouping separators', () => {
      expect(formatNumber(1234)).toBe('1,234');
    });

    it('formats zero as 0', () => {
      expect(formatNumber(0)).toBe('0');
    });

    it('formats decimals with up to one fractional digit', () => {
      expect(formatNumber(1234.5)).toBe('1,234.5');
    });
  });
});
