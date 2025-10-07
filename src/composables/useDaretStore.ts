import { computed } from 'vue';
import { useStorage } from '@vueuse/core';
import dayjs from 'dayjs';
import { nanoid } from 'nanoid';
import { z } from 'zod';

import daretsSeed from '@/data/darets.mock.json';
import type { Daret, DaretEtat, Membre, PaiementStatut, Round } from '@/types';
import { computeRoundDates, startOfNextMonth } from './useDates';
import { ensureRoster, randomizeRoster, validateRoster } from '@/utils/roster';

const memberSchema = z.object({
  id: z.string(),
  nom: z.string().min(1, 'Le nom est requis'),
  contact: z
    .string()
    .trim()
    .optional()
    .transform((value) => (value ? value : undefined)),
});

const reglesSchema = z.object({
  delaiGraceJours: z.number().int().min(0).max(30),
  rappelLocal: z.boolean(),
});

const daretCreationSchema = z
  .object({
    nom: z.string().min(3, 'Le nom doit contenir au moins 3 caractères'),
    description: z
      .string()
      .trim()
      .optional()
      .transform((value) => (value ? value : undefined)),
    devise: z.enum(['MAD', 'EUR']),
    montantMensuel: z
      .number({ invalid_type_error: 'Le montant doit être un nombre' })
      .min(10, 'Le montant minimum est 10'),
    taille: z
      .number({ invalid_type_error: 'La taille doit être un nombre' })
      .int('La taille doit être un nombre entier')
      .min(2, 'Minimum 2 membres')
      .max(50, 'Maximum 50 membres'),
    createurId: z.string(),
    membres: z.array(memberSchema),
    roster: z.array(z.string()),
    visibilite: z.enum(['privee', 'non-listee', 'publique']),
    dateDebut: z.string().optional(),
    regles: reglesSchema,
  })
  .superRefine((value, ctx) => {
    if (!validateRoster(value.roster, value.taille)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Le roster doit contenir tous les membres sans doublon',
        path: ['roster'],
      });
    }

    if (value.membres.length > value.taille) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Trop de membres par rapport à la taille déclarée',
        path: ['membres'],
      });
    }

    const minimumDate = dayjs().startOf('day');
    if (value.dateDebut) {
      const provided = dayjs(value.dateDebut);
      if (!provided.isValid() || provided.isBefore(dayjs().startOf('day'))) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'La date de début doit être dans le futur (début de mois)',
          path: ['dateDebut'],
        });
      }
    } else {
      value.dateDebut = startOfNextMonth().format('YYYY-MM-DD');
    }

    const startDate = dayjs(value.dateDebut);
    if (startDate.isBefore(minimumDate)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'La date de début doit être dans le futur (début de mois)',
        path: ['dateDebut'],
      });
    }
  });

export type CreateDaretInput = z.infer<typeof daretCreationSchema>;

const daretsStorage = useStorage<Daret[]>(
  'tonti:darets',
  (typeof structuredClone === 'function'
    ? structuredClone(daretsSeed)
    : JSON.parse(JSON.stringify(daretsSeed))) as Daret[],
);

function generateRounds(roster: string[], startDateISO: string): Round[] {
  return roster.map((memberId, index) => {
    const { dateDebut, dateFin } = computeRoundDates(index + 1, startDateISO);
    return {
      index: index + 1,
      receveurId: memberId,
      dateDebut,
      dateFin,
      clos: false,
    } satisfies Round;
  });
}

function ensurePayments(daret: Daret, roundIndex: number) {
  const round = daret.rounds.find((item) => item.index === roundIndex);
  if (!round) {
    return;
  }

  daret.membres.forEach((membre) => {
    const existing = daret.paiements.find(
      (paiement) => paiement.membreId === membre.id && paiement.round === roundIndex,
    );
    if (!existing) {
      daret.paiements.push({
        membreId: membre.id,
        round: roundIndex,
        montant: daret.montantMensuel,
        statut: 'a_payer',
        dateMaj: new Date().toISOString(),
      });
    }
  });
}

export function useDaretStore() {
  const darets = daretsStorage;

  function listDarets() {
    return darets.value;
  }

  function getDaret(id: string) {
    return darets.value.find((daret) => daret.id === id);
  }

  function persist() {
    darets.value = [...darets.value];
  }

  function createDaret(input: CreateDaretInput) {
    const parsed = daretCreationSchema.parse(input);

    const start = parsed.dateDebut ?? startOfNextMonth().format('YYYY-MM-DD');
    const roster = ensureRoster(parsed.roster, parsed.membres);
    const rounds = generateRounds(roster, start);
    const initialEtat: DaretEtat = roster.length >= parsed.taille ? 'verrouillee' : 'recrutement';

    const daret: Daret = {
      id: nanoid(10),
      nom: parsed.nom,
      description: parsed.description,
      devise: parsed.devise,
      montantMensuel: parsed.montantMensuel,
      taille: parsed.taille,
      etat: initialEtat,
      createurId: parsed.createurId,
      membres: parsed.membres,
      roster,
      rounds,
      paiements: [],
      visibilite: parsed.visibilite,
      codeInvitation: nanoid(6).toUpperCase(),
      dateCreation: new Date().toISOString(),
      dateDebut: start,
      regles: parsed.regles,
    };

    ensurePayments(daret, 1);
    darets.value = [...darets.value, daret];
    return daret;
  }

  function updateDaret(daret: Daret) {
    const index = darets.value.findIndex((item) => item.id === daret.id);
    if (index >= 0) {
      darets.value.splice(index, 1, daret);
      persist();
    }
  }

  function markPaiement(daretId: string, membreId: string, roundIndex: number, statut: PaiementStatut) {
    const daret = getDaret(daretId);
    if (!daret) {
      return;
    }

    const paiement = daret.paiements.find(
      (item) => item.membreId === membreId && item.round === roundIndex,
    );
    if (paiement) {
      paiement.statut = statut;
      paiement.dateMaj = new Date().toISOString();
    }

    updateEtat(daret);
    updateDaret(daret);
  }

  function updateEtat(daret: Daret) {
    if (daret.etat === 'terminee') {
      return;
    }

    if (daret.membres.length >= daret.taille && daret.etat === 'recrutement') {
      daret.etat = 'verrouillee';
    }

    const allRoundsClosed = daret.rounds.every((round) => round.clos);
    if (allRoundsClosed) {
      daret.etat = 'terminee';
    } else if (daret.rounds.some((round) => !round.clos)) {
      daret.etat = 'active';
    }
  }

  function closeRound(daretId: string, roundIndex: number) {
    const daret = getDaret(daretId);
    if (!daret) {
      return;
    }

    const round = daret.rounds.find((item) => item.index === roundIndex);
    if (!round) {
      return;
    }

    const payments = daret.paiements.filter((paiement) => paiement.round === roundIndex);
    const allPaid = payments.every((paiement) => paiement.statut === 'paye');
    if (!allPaid) {
      return;
    }

    round.clos = true;

    const nextRound = daret.rounds.find((item) => item.index === roundIndex + 1);
    if (nextRound) {
      nextRound.clos = false;
      ensurePayments(daret, nextRound.index);
    }

    updateEtat(daret);
    updateDaret(daret);
  }

  function joinDaretByCode(code: string, membre: Membre) {
    const daret = darets.value.find((item) => item.codeInvitation === code.trim());
    if (!daret) {
      return null;
    }

    if (daret.etat !== 'recrutement') {
      return null;
    }

    daret.membres.push(membre);
    daret.roster.push(membre.id);
    if (daret.membres.length >= daret.taille) {
      daret.etat = 'verrouillee';
    }

    updateDaret(daret);
    return daret;
  }

  const allDarets = computed(() => darets.value);

  return {
    allDarets,
    listDarets,
    getDaret,
    createDaret,
    markPaiement,
    closeRound,
    joinDaretByCode,
    randomizeRoster,
  };
}

export { daretCreationSchema };
