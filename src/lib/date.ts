import type { Locale } from "./i18n";

function ensureDate(value: string): Date {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return new Date();
  }
  return parsed;
}

const phrases = {
  fr: {
    seconds: "à l'instant",
    minute: "il y a une minute",
    minutes: (value: number) => `il y a ${value} minutes`,
    hour: "il y a une heure",
    hours: (value: number) => `il y a ${value} heures`,
    day: "il y a un jour",
    days: (value: number) => `il y a ${value} jours`
  },
  ar: {
    seconds: "الآن",
    minute: "منذ دقيقة",
    minutes: (value: number) => `منذ ${value} دقيقة`,
    hour: "منذ ساعة",
    hours: (value: number) => `منذ ${value} ساعة`,
    day: "منذ يوم",
    days: (value: number) => `منذ ${value} يوم`
  }
};

export function formatRelative(date: string, locale: Locale = "fr"): string {
  const target = ensureDate(date);
  const now = new Date();
  const diffMs = Math.max(now.getTime() - target.getTime(), 0);
  const diffMinutes = Math.round(diffMs / 60000);
  const diffHours = Math.round(diffMinutes / 60);
  const diffDays = Math.round(diffHours / 24);
  const text = phrases[locale];

  if (diffMinutes <= 1) return text.seconds;
  if (diffMinutes < 60) return text.minutes(diffMinutes);
  if (diffHours === 1) return text.hour;
  if (diffHours < 24) return text.hours(diffHours);
  if (diffDays === 1) return text.day;
  return text.days(diffDays);
}

export function daysRemaining(date: string): number {
  const target = ensureDate(date);
  const now = new Date();
  const diffMs = target.getTime() - now.getTime();
  return Math.max(Math.ceil(diffMs / (1000 * 60 * 60 * 24)), 0);
}
