import { describe, expect, it } from 'vitest';

import type { Membre } from '@/types';
import { randomizeRoster, validateRoster } from '../roster';

describe('roster utilities', () => {
  const membres: Membre[] = [
    { id: '1', nom: 'A' },
    { id: '2', nom: 'B' },
    { id: '3', nom: 'C' },
  ];

  it('randomizes roster with deterministic seed', () => {
    const seed = 'seed-123';
    const resultA = randomizeRoster(membres, seed);
    const resultB = randomizeRoster(membres, seed);
    expect(resultA).toEqual(resultB);
    expect(resultA).toHaveLength(membres.length);
  });

  it('validates roster size and uniqueness', () => {
    expect(validateRoster(['1', '2', '3'], 3)).toBe(true);
    expect(validateRoster(['1', '1', '2'], 3)).toBe(false);
    expect(validateRoster(['1', '2'], 3)).toBe(false);
  });
});
