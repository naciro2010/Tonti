import type { Membre } from '@/types';

function stringToSeed(seed: string) {
  let h = 1779033703 ^ seed.length;
  for (let i = 0; i < seed.length; i += 1) {
    h = Math.imul(h ^ seed.charCodeAt(i), 3432918353);
    h = (h << 13) | (h >>> 19);
  }
  return () => {
    h = Math.imul(h ^ (h >>> 16), 2246822507);
    h = Math.imul(h ^ (h >>> 13), 3266489909);
    const t = (h ^= h >>> 16) >>> 0;
    return (t & 0xfffffff) / 0x10000000;
  };
}

export function randomizeRoster(members: Membre[], seed: string) {
  const rand = stringToSeed(seed);
  const pool = [...members];
  const randomized: Membre[] = [];

  while (pool.length) {
    const index = Math.floor(rand() * pool.length);
    randomized.push(pool.splice(index, 1)[0]);
  }

  return randomized.map((member) => member.id);
}

export function validateRoster(roster: string[], expectedSize: number) {
  if (roster.length !== expectedSize) {
    return false;
  }

  const unique = new Set(roster);
  return unique.size === roster.length;
}

export function ensureRoster(roster: string[], members: Membre[]) {
  const memberIds = new Set(members.map((member) => member.id));
  return roster.filter((id) => memberIds.has(id));
}
