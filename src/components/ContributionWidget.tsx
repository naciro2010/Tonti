import { useState } from 'react';
import { Modal } from './ui/Modal';
import { Button } from './ui/Button';
import { Input } from './ui/Input';
import { useToast } from './ui/Toast';
import { Progress } from './ui/Progress';
import { useTranslation } from '../lib/useTranslation';

type ContributionWidgetProps = {
  goal: number;
  raised: number;
  minContribution: number;
  quickOptions?: number[];
  slug: string;
};

export function ContributionWidget({ goal, raised, minContribution, quickOptions, slug }: ContributionWidgetProps) {
  const { t } = useTranslation();
  const { push } = useToast();
  const [open, setOpen] = useState(false);
  const [amount, setAmount] = useState(minContribution);
  const [anonymous, setAnonymous] = useState(false);
  const [loading, setLoading] = useState(false);

  const handlePayment = async () => {
    setLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 900));
    try {
      await fetch('/api/payment/simulate');
      push({
        title: t('contribute.simulated.success'),
        description: `${amount} MAD`,
        variant: 'success',
      });
      setOpen(false);
    } catch (error) {
      push({ title: t('contribute.simulated.error'), description: 'Réessayez plus tard.', variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 rounded-3xl border border-white/10 bg-white/[0.03] p-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-white/60">{t('contribute.collected')}</p>
          <p className="text-3xl font-semibold text-white">{raised.toLocaleString('fr-MA')} MAD</p>
        </div>
        <div className="text-right text-sm text-white/60">
          <p>{t('contribute.goal')}</p>
          <p className="font-semibold text-white">{goal.toLocaleString('fr-MA')} MAD</p>
        </div>
      </div>
      <Progress value={raised} max={goal} />
      <Button className="w-full" onClick={() => setOpen(true)}>
        {t('contribute.cta')}
      </Button>
      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title={t('contribute.modal.title')}
        description={t('contribute.modal.description')}
      >
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            {(quickOptions ?? [50, 100, 200]).map((option) => (
              <Button
                key={option}
                type="button"
                variant={amount === option ? 'primary' : 'ghost'}
                onClick={() => setAmount(option)}
                className="justify-center"
              >
                {option} MAD
              </Button>
            ))}
          </div>
          <Input
            type="number"
            label={t('contribute.amount.placeholder')}
            min={minContribution}
            value={amount}
            onChange={(event) => setAmount(Number(event.target.value))}
          />
          <label className="flex items-center space-x-2 text-sm text-white/80">
            <input
              type="checkbox"
              checked={anonymous}
              onChange={(event) => setAnonymous(event.target.checked)}
              className="h-4 w-4 rounded border-white/30 bg-transparent text-primary focus:ring-primary"
            />
            <span>{t('contribute.anonymous')}</span>
          </label>
          <Button onClick={handlePayment} disabled={loading} className="w-full">
            {loading ? 'Simulation...' : t('contribute.pay')}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
