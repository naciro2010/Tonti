import dayjs from 'dayjs';
import advancedFormat from 'dayjs/plugin/advancedFormat';
import localizedFormat from 'dayjs/plugin/localizedFormat';
import updateLocale from 'dayjs/plugin/updateLocale';

import type { Round } from '@/types';

dayjs.extend(advancedFormat);
dayjs.extend(localizedFormat);
dayjs.extend(updateLocale);

dayjs.updateLocale('fr', {
  weekStart: 1,
});

dayjs.updateLocale('en', {
  weekStart: 1,
});

export function startOfNextMonth(from = dayjs()) {
  return from.add(1, 'month').startOf('month');
}

export function endOfMonth(from: dayjs.Dayjs) {
  return from.endOf('month');
}

export function iso(date: dayjs.Dayjs | string | Date) {
  return dayjs(date).toISOString();
}

export function formatDate(date: string, locale = 'fr') {
  return dayjs(date).locale(locale).format('D MMM YYYY');
}

export function daysRemaining(round: Round) {
  const today = dayjs();
  const end = dayjs(round.dateFin);
  const diff = end.diff(today, 'day');
  return diff >= 0 ? diff : 0;
}

export function currentRound(rounds: Round[]) {
  return rounds.find((round) => !round.clos) ?? rounds[rounds.length - 1];
}

export function computeRoundDates(index: number, startDateISO: string) {
  const start = dayjs(startDateISO).add(index - 1, 'month').startOf('month');
  const end = start.clone().endOf('month');
  return {
    dateDebut: start.format('YYYY-MM-DD'),
    dateFin: end.format('YYYY-MM-DD'),
  };
}
