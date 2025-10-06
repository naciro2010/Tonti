import { Button } from './ui/Button';
import { useTranslation, setLocale } from '../lib/useTranslation';

export function LanguageToggle() {
  const { locale, t } = useTranslation();

  const handleToggle = () => {
    const next = locale === 'fr' ? 'ar' : 'fr';
    setLocale(next);
  };

  return (
    <Button variant="ghost" onClick={handleToggle} className="text-xs font-semibold uppercase tracking-wide">
      {t('toggle.language')}
    </Button>
  );
}
