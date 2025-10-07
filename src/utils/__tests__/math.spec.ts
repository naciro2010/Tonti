import { describe, expect, it } from 'vitest';

import type { Paiement } from '@/types';
import { calculateRoundTotal, calculateTotalPot, completionPercentage, countByStatut, isRoundClosable } from '../math';

describe('math utilities', () => {
  const paiements: Paiement[] = [
    { membreId: 'a', round: 1, montant: 100, statut: 'paye', dateMaj: '2024-06-01' },
    { membreId: 'b', round: 1, montant: 100, statut: 'retard', dateMaj: '2024-06-02' },
    { membreId: 'c', round: 1, montant: 100, statut: 'paye', dateMaj: '2024-06-03' },
  ];

  it('calculates total pot', () => {
    expect(calculateTotalPot({ taille: 10, montantMensuel: 100 })).toBe(1000);
  });

  it('computes round totals and percentages', () => {
    expect(calculateRoundTotal(paiements)).toBe(300);
    expect(countByStatut(paiements, 'retard')).toBe(1);
    expect(completionPercentage(paiements)).toBeCloseTo(67);
  });

  it('detects closable rounds', () => {
    expect(isRoundClosable(paiements)).toBe(false);
    const allPaid = paiements.map((paiement) => ({ ...paiement, statut: 'paye' as const }));
    expect(isRoundClosable(allPaid)).toBe(true);
  });
});
