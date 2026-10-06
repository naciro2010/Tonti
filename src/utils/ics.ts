import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';

import type { DaretDetailResponse } from '@/services/api';

dayjs.extend(utc);

function formatIcsDate(date: string | Date) {
  return dayjs(date).utc().format('YYYYMMDD');
}

/** Échappement des valeurs texte iCalendar (RFC 5545 §3.3.11). */
function escapeText(value: string) {
  return value.replace(/\\/g, '\\\\').replace(/;/g, '\\;').replace(/,/g, '\\,').replace(/\r?\n/g, '\\n');
}

/** Calendrier .ics : un événement par round (période de cotisation et bénéficiaire). */
export function buildIcs(daret: DaretDetailResponse, now: Date = new Date()) {
  const lines = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//Tonti//Daret//FR', 'CALSCALE:GREGORIAN'];

  daret.rounds.forEach((round) => {
    const receveur = `${round.receveur.firstName} ${round.receveur.lastName}`;
    lines.push(
      'BEGIN:VEVENT',
      `UID:${daret.id}-round-${round.numero}@tonti`,
      `DTSTAMP:${dayjs(now).utc().format('YYYYMMDD[T]HHmmss[Z]')}`,
      `DTSTART;VALUE=DATE:${formatIcsDate(round.dateDebut)}`,
      `DTEND;VALUE=DATE:${formatIcsDate(round.dateFin)}`,
      `SUMMARY:${escapeText(`${daret.nom} · Round ${round.numero}`)}`,
      `DESCRIPTION:${escapeText(`Bénéficiaire : ${receveur}`)}`,
      'END:VEVENT',
    );
  });

  lines.push('END:VCALENDAR');
  return lines.join('\r\n');
}
