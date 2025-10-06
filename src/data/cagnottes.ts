import type { CategoryId } from "./categories";

export type Contribution = {
  nom: string;
  montantMAD: number;
  date: string;
  statut: "completed" | "pending";
};

export type Organisateur = { id: string; nom: string };

export type Beneficiaire = { nom: string };

export type Cagnotte = {
  id: string;
  titre: string;
  description: string;
  categorie: CategoryId;
  organisateur: Organisateur;
  beneficiaire: Beneficiaire;
  objectifMAD: number;
  collecteMAD: number;
  participants: number;
  dateFin: string;
  visibilite: "publique" | "privee" | "non-listee";
  imageCover: string;
  historique: Contribution[];
  statut: "ouverte" | "close";
};

export const cagnottes: Cagnotte[] = [
  {
    id: "soutien-rif",
    titre: "Solidarité pour le Rif",
    description: "Aidons les familles touchées par les intempéries dans le Rif.",
    categorie: "solidarite",
    organisateur: { id: "u1", nom: "Association Rif Solidaire" },
    beneficiaire: { nom: "Familles du Rif" },
    objectifMAD: 50000,
    collecteMAD: 32400,
    participants: 268,
    dateFin: "2024-12-31",
    visibilite: "publique",
    imageCover: "/images/cover1.svg",
    historique: [
      { nom: "Nadia", montantMAD: 500, date: "2024-06-02", statut: "completed" },
      { nom: "Anonyme", montantMAD: 200, date: "2024-06-02", statut: "completed" },
      { nom: "Youssef", montantMAD: 150, date: "2024-06-01", statut: "completed" }
    ],
    statut: "ouverte"
  },
  {
    id: "mariage-samia-youssef",
    titre: "Mariage de Samia & Youssef",
    description: "Une cagnotte pour célébrer l'union de Samia et Youssef à Fès.",
    categorie: "mariage",
    organisateur: { id: "u2", nom: "Amis de Samia" },
    beneficiaire: { nom: "Samia & Youssef" },
    objectifMAD: 20000,
    collecteMAD: 9600,
    participants: 84,
    dateFin: "2024-09-15",
    visibilite: "publique",
    imageCover: "/images/cover2.svg",
    historique: [
      { nom: "Karim", montantMAD: 300, date: "2024-06-01", statut: "completed" },
      { nom: "Anonyme", montantMAD: 150, date: "2024-05-30", statut: "completed" }
    ],
    statut: "ouverte"
  }
];
