import dayjs from 'dayjs';
import { describe, expect, it } from 'vitest';

import { daysUntil, formatDate } from '../useDates';

describe('useDates', () => {
  it('formats a date in French', () => {
    expect(formatDate('2025-03-15T10:00:00Z', 'fr')).toBe('15 mars 2025');
  });

  it('counts the days remaining until a date', () => {
    expect(daysUntil('2025-03-20T00:00:00', dayjs('2025-03-15T18:00:00'))).toBe(5);
  });

  it('never returns a negative number of days', () => {
    expect(daysUntil('2025-03-10T00:00:00', dayjs('2025-03-15T00:00:00'))).toBe(0);
  });
});
