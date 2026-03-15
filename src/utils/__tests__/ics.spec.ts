import { describe, expect, it } from 'vitest'

import type { Daret } from '@/types'
import { buildIcs } from '../ics'

describe('buildIcs', () => {
  const daret: Daret = {
    id: 'test-daret-1',
    nom: 'Test Daret',
    devise: 'MAD',
    montantMensuel: 500,
    taille: 3,
    etat: 'active',
    createurId: 'user-1',
    membres: [
      { id: 'a', nom: 'Alice' },
      { id: 'b', nom: 'Bob' },
      { id: 'c', nom: 'Charlie' },
    ],
    roster: ['a', 'b', 'c'],
    rounds: [
      { index: 1, receveurId: 'a', dateDebut: '2025-06-01', dateFin: '2025-06-30', clos: true },
      { index: 2, receveurId: 'b', dateDebut: '2025-07-01', dateFin: '2025-07-31', clos: false },
      { index: 3, receveurId: 'c', dateDebut: '2025-08-01', dateFin: '2025-08-31', clos: false },
    ],
    paiements: [],
    visibilite: 'privee',
    codeInvitation: 'ABC123',
    dateCreation: '2025-05-01T00:00:00Z',
    regles: { delaiGraceJours: 3, rappelLocal: true },
  }

  it('produces valid iCalendar format', () => {
    const ics = buildIcs(daret)
    expect(ics).toContain('BEGIN:VCALENDAR')
    expect(ics).toContain('END:VCALENDAR')
    expect(ics).toContain('VERSION:2.0')
    expect(ics).toContain('PRODID:-//Tonti//Daret//FR')
  })

  it('creates one VEVENT per round', () => {
    const ics = buildIcs(daret)
    const eventCount = (ics.match(/BEGIN:VEVENT/g) || []).length
    expect(eventCount).toBe(3)
  })

  it('includes daret name and round number in summary', () => {
    const ics = buildIcs(daret)
    expect(ics).toContain('SUMMARY:Test Daret \u00b7 Round 1')
    expect(ics).toContain('SUMMARY:Test Daret \u00b7 Round 2')
  })

  it('includes receiver name in description', () => {
    const ics = buildIcs(daret)
    expect(ics).toContain('DESCRIPTION:Receveur: Alice')
    expect(ics).toContain('DESCRIPTION:Receveur: Bob')
  })

  it('generates unique UIDs per round', () => {
    const ics = buildIcs(daret)
    expect(ics).toContain('UID:test-daret-1-round-1@tonti')
    expect(ics).toContain('UID:test-daret-1-round-2@tonti')
  })

  it('uses CRLF line endings', () => {
    const ics = buildIcs(daret)
    expect(ics).toContain('\r\n')
  })
})
