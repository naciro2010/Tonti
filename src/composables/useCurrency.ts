import type { Currency } from '@/services/api';

const LOCALES: Record<Currency, string> = {
  MAD: 'fr-MA',
  EUR: 'fr-FR',
  USD: 'en-US',
};

export function formatCurrency(amount: number, currency: Currency, locale?: string) {
  return new Intl.NumberFormat(locale ?? LOCALES[currency], {
    style: 'currency',
    currency,
    currencyDisplay: 'symbol',
    maximumFractionDigits: 2,
  }).format(amount);
}
