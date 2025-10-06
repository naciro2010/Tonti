export interface Cagnotte {
  slug: string;
  title: string;
  category: string;
  organizer: string;
  goal: number;
  raised: number;
  coverImage: string;
  visibility: 'public' | 'private' | 'unlisted';
  deadline: string;
  description: string;
  quickOptions: number[];
  minimum: number;
}
