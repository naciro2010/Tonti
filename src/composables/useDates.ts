import dayjs from 'dayjs';
import 'dayjs/locale/ar';
import 'dayjs/locale/fr';

export function formatDate(date: string, locale = 'fr') {
  return dayjs(date).locale(locale).format('D MMM YYYY');
}

export function formatDateTime(date: string, locale = 'fr') {
  return dayjs(date).locale(locale).format('D MMM YYYY · HH:mm');
}

/** Nombre de jours restants avant la date donnée (0 si elle est passée). */
export function daysUntil(date: string, now = dayjs()) {
  const diff = dayjs(date).startOf('day').diff(now.startOf('day'), 'day');
  return Math.max(diff, 0);
}
