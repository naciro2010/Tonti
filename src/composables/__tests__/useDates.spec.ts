import { describe, expect, it } from 'vitest'
import dayjs from 'dayjs'

import { startOfNextMonth, computeRoundDates, formatDate, daysRemaining } from '../useDates'
import type { Round } from '@/types'

describe('useDates', () => {
  describe('startOfNextMonth', () => {
    it('returns the first day of next month', () => {
      const from = dayjs('2025-03-15')
      const result = startOfNextMonth(from)
      expect(result.format('YYYY-MM-DD')).toBe('2025-04-01')
    })

    it('handles year rollover', () => {
      const from = dayjs('2025-12-20')
      const result = startOfNextMonth(from)
      expect(result.format('YYYY-MM-DD')).toBe('2026-01-01')
    })
  })

  describe('computeRoundDates', () => {
    it('computes correct dates for round 1', () => {
      const result = computeRoundDates(1, '2025-06-01')
      expect(result.dateDebut).toBe('2025-06-01')
      expect(result.dateFin).toBe('2025-06-30')
    })

    it('computes correct dates for round 3', () => {
      const result = computeRoundDates(3, '2025-06-01')
      expect(result.dateDebut).toBe('2025-08-01')
      expect(result.dateFin).toBe('2025-08-31')
    })

    it('handles year boundary', () => {
      const result = computeRoundDates(2, '2025-12-01')
      expect(result.dateDebut).toBe('2026-01-01')
      expect(result.dateFin).toBe('2026-01-31')
    })
  })

  describe('formatDate', () => {
    it('formats date in French locale', () => {
      const result = formatDate('2025-06-15', 'fr')
      expect(result).toContain('15')
      expect(result).toContain('2025')
    })
  })

  describe('daysRemaining', () => {
    it('returns 0 when round has ended', () => {
      const round: Round = {
        index: 1,
        receveurId: 'a',
        dateDebut: '2020-01-01',
        dateFin: '2020-01-31',
        clos: false,
      }
      expect(daysRemaining(round)).toBe(0)
    })

    it('returns positive days for future round end', () => {
      const futureEnd = dayjs().add(10, 'day').format('YYYY-MM-DD')
      const round: Round = {
        index: 1,
        receveurId: 'a',
        dateDebut: '2025-01-01',
        dateFin: futureEnd,
        clos: false,
      }
      expect(daysRemaining(round)).toBeGreaterThanOrEqual(9)
    })
  })
})
