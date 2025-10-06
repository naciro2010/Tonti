import { useState } from 'react';
import { Modal } from './ui/Modal';
import { Button } from './ui/Button';
import { Input } from './ui/Input';
import { useTranslation } from '../lib/useTranslation';

export function LoginModal() {
  const { t } = useTranslation();
  const [open, setOpen] = useState(true);
  const [email, setEmail] = useState('');

  return (
    <>
      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title={t('auth.login.title')}
        description={t('auth.login.description')}
        primaryAction={{ label: t('contribute.pay'), onClick: () => setOpen(false) }}
      >
        <div className="space-y-4">
          <Input
            label={t('auth.login.email')}
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="votre@email.ma"
          />
          <Button type="button" className="w-full" onClick={() => setOpen(false)}>
            {t('auth.login.magic')}
          </Button>
          <div className="space-y-2 text-xs text-white/50">
            <p>{t('auth.login.socialSoon')}</p>
            <div className="grid gap-2 sm:grid-cols-2">
              <button
                type="button"
                disabled
                className="flex items-center justify-center rounded-lg border border-white/10 bg-white/5 px-4 py-2 text-sm text-white/40"
              >
                {t('auth.login.google')}
              </button>
              <button
                type="button"
                disabled
                className="flex items-center justify-center rounded-lg border border-white/10 bg-white/5 px-4 py-2 text-sm text-white/40"
              >
                {t('auth.login.apple')}
              </button>
            </div>
          </div>
        </div>
      </Modal>
      <div className="flex justify-center">
        <Button onClick={() => setOpen(true)}>{t('auth.login.open')}</Button>
      </div>
    </>
  );
}
