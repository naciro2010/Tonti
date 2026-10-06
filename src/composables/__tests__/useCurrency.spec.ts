import { describe, expect, it } from 'vitest';

import { formatCurrency } from '../useCurrency';

describe('formatCurrency', () => {
  it('formats MAD amounts', () => {
    const result = formatCurrency(500, 'MAD');
    expect(result).toContain('500');
    expect(result).toMatch(/MAD|DH/);
  });

  it('formats EUR amounts', () => {
    expect(formatCurrency(100, 'EUR')).toContain('€');
  });

  it('formats USD amounts', () => {
    expect(formatCurrency(12.5, 'USD')).toBe('$12.50');
  });

  it('keeps at most two decimals', () => {
    expect(formatCurrency(99.999, 'EUR')).toContain('100');
  });
});
