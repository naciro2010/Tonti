import { describe, expect, it } from 'vitest';

import type { DaretDetailResponse, MembreResponse } from '@/services/api';

import { buildIcs } from '../ics';

const membre = (id: string, firstName: string, position: number): MembreResponse => ({
  id,
  userId: `user-${id}`,
  firstName,
  lastName: 'Test',
  role: position === 1 ? 'CREATEUR' : 'MEMBRE',
  position,
  isActive: true,
  joinedAt: '2025-05-01T00:00:00Z',
});

const alice = membre('a', 'Alice', 1);
const bob = membre('b', 'Bob', 2);

const round = (numero: number, receveur: MembreResponse, debut: string, fin: string) => ({
  id: `round-${numero}`,
  numero,
  receveur,
  dateDebut: debut,
  dateFin: fin,
  estClos: false,
  montantTotal: 0,
  paymentsCount: 0,
  paidCount: 0,
  paidUserIds: [],
});

const daret: DaretDetailResponse = {
  id: 'test-daret-1',
  nom: 'Daret, famille',
  devise: 'MAD',
  montantMensuel: 500,
  taille: 2,
  etat: 'ACTIVE',
  visibilite: 'PRIVEE',
  codeInvitation: 'ABC234',
  delaiGraceJours: 3,
  createur: alice,
  membres: [alice, bob],
  rounds: [
    round(1, alice, '2025-06-01T00:00:00Z', '2025-07-01T00:00:00Z'),
    round(2, bob, '2025-07-01T00:00:00Z', '2025-07-31T00:00:00Z'),
  ],
  createdAt: '2025-05-01T00:00:00Z',
};

describe('buildIcs', () => {
  const ics = buildIcs(daret, new Date('2025-05-02T10:00:00Z'));

  it('produces a valid iCalendar envelope with CRLF line endings', () => {
    expect(ics.startsWith('BEGIN:VCALENDAR\r\nVERSION:2.0')).toBe(true);
    expect(ics.endsWith('END:VCALENDAR')).toBe(true);
  });

  it('creates one event per round with a stable UID', () => {
    expect(ics.match(/BEGIN:VEVENT/g)).toHaveLength(2);
    expect(ics).toContain('UID:test-daret-1-round-1@tonti');
    expect(ics).toContain('UID:test-daret-1-round-2@tonti');
  });

  it('uses all-day dates and a UTC timestamp', () => {
    expect(ics).toContain('DTSTART;VALUE=DATE:20250601');
    expect(ics).toContain('DTSTAMP:20250502T100000Z');
  });

  it('escapes text values and names the beneficiary', () => {
    expect(ics).toContain('SUMMARY:Daret\\, famille · Round 1');
    expect(ics).toContain('DESCRIPTION:Bénéficiaire : Bob Test');
  });
});
