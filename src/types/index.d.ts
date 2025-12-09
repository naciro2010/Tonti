export type Devise = 'MAD' | 'EUR';
export type PaiementStatut = 'a_payer' | 'paye' | 'retard';

export type UserRole = 'createur' | 'membre' | 'viewer';

export type PaymentMethod = 'stripe' | 'paypal' | 'crypto' | 'mobile_money' | 'bank_transfer' | 'cash';

export interface PaymentInfo {
  method: PaymentMethod;
  details: {
    stripeLink?: string;
    paypalEmail?: string;
    cryptoAddress?: {
      network: 'BTC' | 'ETH' | 'USDT';
      address: string;
    };
    phoneNumber?: string; // pour mobile money
    iban?: string;
    accountName?: string;
  };
}

export interface UserSession {
  userId: string;
  userName: string;
  role: UserRole;
  daretId: string;
  joinedAt: string;
}

export interface Membre {
  id: string;
  nom: string;
  contact?: string;
  paymentInfo?: PaymentInfo;
}

export interface Paiement {
  membreId: string;
  round: number;
  montant: number;
  statut: PaiementStatut;
  dateMaj: string;
}

export interface Round {
  index: number;
  receveurId: string;
  dateDebut: string;
  dateFin: string;
  clos: boolean;
}

export type DaretEtat = 'recrutement' | 'verrouillee' | 'active' | 'terminee';

export interface DaretRegles {
  delaiGraceJours: number;
  rappelLocal: boolean;
}

export interface Daret {
  id: string;
  nom: string;
  description?: string;
  devise: Devise;
  montantMensuel: number;
  taille: number;
  etat: DaretEtat;
  createurId: string;
  membres: Membre[];
  roster: string[];
  rounds: Round[];
  paiements: Paiement[];
  visibilite: 'privee' | 'non-listee' | 'publique';
  codeInvitation: string;
  dateCreation: string;
  dateDebut?: string;
  regles: DaretRegles;
}
