import { describe, expect, it } from 'vitest'

import { formatCurrency } from '../useCurrency'

describe('useCurrency', () => {
  describe('formatCurrency', () => {
    it('formats MAD amounts with Moroccan locale', () => {
      const result = formatCurrency(500, 'MAD')
      expect(result).toContain('500')
      expect(result).toContain('MAD')
    })

    it('formats EUR amounts with French locale', () => {
      const result = formatCurrency(100, 'EUR')
      expect(result).toContain('100')
    })

    it('formats decimal amounts correctly', () => {
      const result = formatCurrency(99.99, 'MAD')
      expect(result).toContain('99')
    })

    it('formats zero amount', () => {
      const result = formatCurrency(0, 'MAD')
      expect(result).toContain('0')
    })
  })
})
