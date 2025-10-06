export const formatCurrency = (value: number, locale = 'fr-MA') =>
  new Intl.NumberFormat(locale, { style: 'currency', currency: 'MAD', maximumFractionDigits: 0 }).format(value);

export const formatNumber = (value: number, locale = 'fr-MA') =>
  new Intl.NumberFormat(locale, { maximumFractionDigits: 0 }).format(value);
