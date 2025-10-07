import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';

import type { Daret } from '@/types';

dayjs.extend(utc);

function formatICSDate(date: string) {
  return dayjs(date).utc().format('YYYYMMDD');
}

export function buildIcs(daret: Daret) {
  const lines = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Tonti//Daret//FR',
    'CALSCALE:GREGORIAN',
  ];

  daret.rounds.forEach((round) => {
    const start = formatICSDate(round.dateDebut);
    const end = formatICSDate(round.dateFin);
    const receveur = daret.membres.find((membre) => membre.id === round.receveurId);

    lines.push('BEGIN:VEVENT');
    lines.push(`UID:${daret.id}-round-${round.index}@tonti`);
    lines.push(`DTSTAMP:${formatICSDate(new Date().toISOString())}`);
    lines.push(`DTSTART;VALUE=DATE:${start}`);
    lines.push(`DTEND;VALUE=DATE:${end}`);
    lines.push(`SUMMARY:${daret.nom} · Round ${round.index}`);
    if (receveur) {
      lines.push(`DESCRIPTION:Receveur: ${receveur.nom}`);
    }
    lines.push('END:VEVENT');
  });

  lines.push('END:VCALENDAR');
  return lines.join('\r\n');
}
