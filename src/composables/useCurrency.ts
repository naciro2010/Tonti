import { computed } from 'vue';
import { useStorage } from '@vueuse/core';

import type { Devise } from '@/types';

const MAD_TO_EUR_RATE = 0.091; // taux indicatif, sans appel réseau

export function formatCurrency(amount: number, devise: Devise) {
  return new Intl.NumberFormat(devise === 'MAD' ? 'fr-MA' : 'fr-FR', {
    style: 'currency',
    currency: devise,
    currencyDisplay: 'symbol',
    maximumFractionDigits: 2,
  }).format(amount);
}

export function useCurrency(defaultDevise: Devise) {
  const preference = useStorage<Devise>('tonti:devise-preference', defaultDevise);

  const formatPreferred = (amount: number) => formatCurrency(amount, preference.value ?? defaultDevise);

  const totalLabel = computed(() =>
    preference.value === 'EUR'
      ? '€'
      : preference.value === 'MAD'
        ? 'MAD'
        : defaultDevise
  );

  function toggleDevise() {
    preference.value = preference.value === 'MAD' ? 'EUR' : 'MAD';
  }

  function convert(amount: number, from: Devise, to: Devise) {
    if (from === to) {
      return amount;
    }

    if (from === 'MAD' && to === 'EUR') {
      return +(amount * MAD_TO_EUR_RATE).toFixed(2);
    }

    if (from === 'EUR' && to === 'MAD') {
      return +(amount / MAD_TO_EUR_RATE).toFixed(2);
    }

    return amount;
  }

  function formatBoth(amount: number, devise: Devise) {
    const base = formatCurrency(amount, devise);
    const secondaryDevise = devise === 'MAD' ? 'EUR' : 'MAD';
    const converted = convert(amount, devise, secondaryDevise);
    return `${base} · ${formatCurrency(converted, secondaryDevise)}`;
  }

  return {
    preference,
    totalLabel,
    formatPreferred,
    toggleDevise,
    convert,
    formatBoth,
  };
}
