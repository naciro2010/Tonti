import dayjs from 'dayjs';
import 'dayjs/locale/fr';
import 'dayjs/locale/ar';

export const formatDate = (date: string, locale: 'fr' | 'ar' = 'fr') =>
  dayjs(date).locale(locale === 'ar' ? 'ar' : 'fr').format('D MMMM YYYY');
