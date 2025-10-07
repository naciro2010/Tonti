import type { Daret, Paiement, PaiementStatut } from '@/types';

export function calculateTotalPot(daret: Pick<Daret, 'taille' | 'montantMensuel'>) {
  return daret.taille * daret.montantMensuel;
}

export function calculateRoundTotal(payments: Paiement[]) {
  return payments.reduce((sum, paiement) => sum + paiement.montant, 0);
}

export function countByStatut(payments: Paiement[], statut: PaiementStatut) {
  return payments.filter((paiement) => paiement.statut === statut).length;
}

export function completionPercentage(payments: Paiement[]) {
  if (!payments.length) {
    return 0;
  }
  const paid = countByStatut(payments, 'paye');
  return Math.round((paid / payments.length) * 100);
}

export function isRoundClosable(payments: Paiement[]) {
  return payments.every((paiement) => paiement.statut === 'paye');
}
