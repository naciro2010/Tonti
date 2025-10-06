import { useCallback, useEffect, useMemo, useState } from 'react';
import type { ChangeEvent } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Stepper } from './ui/Stepper';
import { Button } from './ui/Button';
import { Input } from './ui/Input';
import { useToast } from './ui/Toast';
import { useTranslation } from '../lib/useTranslation';
import { QRCodeCanvas } from 'qrcode.react';

const STORAGE_KEY = 'tonti-create-draft';

const schema = z.object({
  title: z.string().min(4),
  description: z.string().min(20),
  category: z.string().min(3),
  coverData: z.string().min(10),
  visibility: z.enum(['public', 'private', 'unlisted']),
  goal: z.number().min(1000),
  minContribution: z.number().min(10),
  quickOptions: z.array(z.number().min(10)).length(3),
  shareMessage: z.string().min(10),
  allowAnonymous: z.boolean().default(true),
});

type FormValues = z.infer<typeof schema> & {
  slug: string;
};

const stepFields: Array<Array<keyof FormValues>> = [
  ['title', 'description', 'category'],
  ['coverData'],
  ['visibility'],
  ['goal', 'minContribution', 'quickOptions'],
  ['shareMessage'],
];

const defaultValues: FormValues = {
  title: '',
  description: '',
  category: '',
  coverData: '',
  visibility: 'public',
  goal: 5000,
  minContribution: 50,
  quickOptions: [50, 100, 200],
  shareMessage: "Rejoins ma cagnotte sur Tonti !",
  allowAnonymous: true,
  slug: '',
};

function toSlug(value: string) {
  return value
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9\u0600-\u06FF\s-]/g, '')
    .replace(/\s+/g, '-');
}

export function CreateCagnotteForm() {
  const { t } = useTranslation();
  const { push } = useToast();
  const [activeStep, setActiveStep] = useState(0);

  const stored = useMemo(() => {
    if (typeof window === 'undefined') return null;
    const draft = window.localStorage.getItem(STORAGE_KEY);
    return draft ? (JSON.parse(draft) as FormValues) : null;
  }, []);

  const {
    register,
    handleSubmit,
    watch,
    trigger,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: stored ?? defaultValues,
    mode: 'onBlur',
  });

  const values = watch();

  useEffect(() => {
    const subscription = watch((value) => {
      const data = { ...value, slug: value.slug ?? toSlug(value.title ?? '') } as FormValues;
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    });
    return () => subscription.unsubscribe();
  }, [watch]);

  useEffect(() => {
    const slug = toSlug(values.title);
    if (slug && slug !== values.slug) {
      setValue('slug', slug, { shouldDirty: true, shouldTouch: true });
    }
  }, [values.title, values.slug, setValue]);

  const goToStep = useCallback(
    async (index: number) => {
      if (index > activeStep) {
        const valid = await trigger(stepFields[activeStep]);
        if (!valid) {
          push({ title: t('form.validation.required'), variant: 'error' });
          return;
        }
      }
      setActiveStep(Math.max(0, Math.min(stepFields.length - 1, index)));
    },
    [activeStep, trigger, push],
  );

  useEffect(() => {
    const handler = async (event: KeyboardEvent) => {
      if (event.key === 'ArrowRight') {
        event.preventDefault();
        await goToStep(activeStep + 1);
      }
      if (event.key === 'ArrowLeft') {
        event.preventDefault();
        await goToStep(activeStep - 1);
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [activeStep, goToStep]);

  const onSubmit = useCallback(
    async (_data: FormValues) => {
      const valid = await trigger(stepFields[activeStep]);
      if (!valid) {
        push({ title: t('form.validation.error'), variant: 'error' });
        return;
      }
      window.localStorage.removeItem(STORAGE_KEY);
      push({ title: t('form.save.success'), variant: 'success' });
    },
    [activeStep, push, t, trigger],
  );

  const handleCoverUpload = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const aspectRatio = 16 / 9;
        const width = img.width;
        const height = img.height;
        const targetWidth = width;
        const targetHeight = width / aspectRatio;
        const sy = Math.max(0, (height - targetHeight) / 2);
        canvas.width = 1280;
        canvas.height = 720;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.drawImage(img, 0, sy, targetWidth, targetHeight, 0, 0, canvas.width, canvas.height);
          const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
          setValue('coverData', dataUrl, { shouldDirty: true });
        }
      };
      if (typeof reader.result === 'string') {
        img.src = reader.result;
      }
    };
    reader.readAsDataURL(file);
  };

  const shareLink = `https://tonti.app/cagnotte/${values.slug || 'nouvelle-cagnotte'}`;

  return (
    <form className="grid gap-6 lg:grid-cols-[280px,1fr]" onSubmit={handleSubmit(onSubmit)}>
      <div className="space-y-4">
        <Stepper
          steps={[
            { id: 1, title: `${t('create.step1.title')}` },
            { id: 2, title: `${t('create.step2.title')}` },
            { id: 3, title: `${t('create.step3.title')}` },
            { id: 4, title: `${t('create.step4.title')}` },
            { id: 5, title: `${t('create.step5.title')}` },
          ]}
          activeStep={activeStep}
          onStepChange={goToStep}
        />
        <p className="text-xs text-white/60">{t('create.autosave')}</p>
      </div>
      <div className="space-y-8">
        {activeStep === 0 ? (
          <section className="space-y-4">
            <Input
              label={t('create.title.label')}
              placeholder="Titre de la cagnotte"
              {...register('title')}
              error={errors.title?.message}
            />
            <label className="flex w-full flex-col space-y-1 text-sm">
              <span className="font-medium text-white/90">{t('create.description.label')}</span>
              <textarea
                className="min-h-[150px] w-full rounded-xl border border-white/20 bg-white/5 px-3 py-2 text-sm text-white placeholder:text-white/40 focus:border-primary focus:ring-primary"
                {...register('description')}
                placeholder="Expliquez votre projet en détail"
              />
              {errors.description ? (
                <span className="text-xs text-red-400">{errors.description.message}</span>
              ) : (
                <span className="text-xs text-white/60">{t('create.description.helper')}</span>
              )}
            </label>
            <Input
              label={t('create.category.label')}
              placeholder="Solidarité"
              {...register('category')}
              error={errors.category?.message}
            />
          </section>
        ) : null}

        {activeStep === 1 ? (
          <section className="space-y-4">
            <div>
              <label className="flex w-full flex-col space-y-2 text-sm">
                <span className="font-medium text-white/90">Illustration (16:9)</span>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleCoverUpload}
                  className="w-full rounded-lg border border-dashed border-white/20 bg-white/5 px-3 py-8 text-center text-white/70 file:hidden"
                />
                {errors.coverData ? (
                  <span className="text-xs text-red-400">{errors.coverData.message}</span>
                ) : (
                  <span className="text-xs text-white/60">
                    Importez une image haute résolution. Le recadrage automatique respecte le ratio 16:9.
                  </span>
                )}
              </label>
            </div>
            {values.coverData ? (
              <div className="overflow-hidden rounded-3xl border border-white/10">
                <img src={values.coverData} alt="Aperçu" className="aspect-video w-full object-cover" />
              </div>
            ) : null}
          </section>
        ) : null}

        {activeStep === 2 ? (
          <section className="space-y-4">
            <fieldset className="grid gap-3">
              <legend className="text-sm font-semibold text-white/80">{t('create.visibility.legend')}</legend>
              {[
                { value: 'public', label: t('create.visibility.public'), helper: t('create.visibility.public.helper') },
                { value: 'private', label: t('create.visibility.private'), helper: t('create.visibility.private.helper') },
                { value: 'unlisted', label: t('create.visibility.unlisted'), helper: t('create.visibility.unlisted.helper') },
              ].map((option) => (
                <label key={option.value} className="flex items-start space-x-3 rounded-2xl border border-white/10 bg-white/[0.03] p-4">
                  <input
                    type="radio"
                    value={option.value}
                    {...register('visibility')}
                    className="mt-1 h-4 w-4 rounded-full border-white/30 bg-transparent text-primary focus:ring-primary"
                  />
                  <span>
                    <span className="block text-sm font-semibold text-white">{option.label}</span>
                    <span className="text-xs text-white/60">{option.helper}</span>
                  </span>
                </label>
              ))}
            </fieldset>
          </section>
        ) : null}

        {activeStep === 3 ? (
          <section className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <Input
                label={t('create.goal.label')}
                type="number"
                min={100}
                {...register('goal', { valueAsNumber: true })}
                error={errors.goal?.message}
              />
              <Input
                label={t('create.minContribution.label')}
                type="number"
                min={10}
                {...register('minContribution', { valueAsNumber: true })}
                error={errors.minContribution?.message}
              />
            </div>
            <div className="space-y-2">
              <span className="text-sm font-medium text-white/90">{t('create.quickOptions.label')}</span>
              <div className="grid gap-3 md:grid-cols-3">
                {values.quickOptions.map((amount, index) => (
                  <Input
                    key={index}
                    type="number"
                    label={`${t('create.quickOptions.label')} ${index + 1}`}
                    {...register(`quickOptions.${index}` as const, { valueAsNumber: true })}
                    error={errors.quickOptions?.[index]?.message}
                  />
                ))}
              </div>
            </div>
          </section>
        ) : null}

        {activeStep === 4 ? (
          <section className="space-y-6">
            <label className="flex w-full flex-col space-y-2 text-sm">
              <span className="font-medium text-white/90">{t('create.invitation.label')}</span>
              <textarea
                className="min-h-[120px] w-full rounded-xl border border-white/20 bg-white/5 px-3 py-2 text-sm text-white placeholder:text-white/40 focus:border-primary focus:ring-primary"
                {...register('shareMessage')}
              />
              {errors.shareMessage ? <span className="text-xs text-red-400">{errors.shareMessage.message}</span> : null}
            </label>
            <div className="space-y-4 rounded-3xl border border-white/10 bg-white/[0.02] p-6">
              <p className="text-sm text-white/70">{t('create.share.helper')}</p>
              <div className="flex flex-col gap-3 md:flex-row md:items-center">
                <code className="flex-1 break-all rounded-2xl bg-black/40 px-4 py-3 text-xs">{shareLink}</code>
                <div className="flex gap-2">
                  <Button
                    type="button"
                    onClick={() => {
                      navigator.clipboard.writeText(shareLink);
                      push({ title: t('share.copy'), variant: 'success' });
                    }}
                  >
                    {t('share.copy')}
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => {
                      const url = new URL('https://wa.me/');
                      url.searchParams.set('text', `${values.shareMessage}\n${shareLink}`);
                      window.open(url.toString(), '_blank');
                    }}
                  >
                    {t('share.whatsapp')}
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => {
                      window.location.href = `mailto:?subject=${encodeURIComponent(values.title)}&body=${encodeURIComponent(`${values.shareMessage}\n${shareLink}`)}`;
                    }}
                  >
                    {t('share.email')}
                  </Button>
                </div>
              </div>
              <div className="flex flex-col items-center space-y-3">
                <QRCodeCanvas value={shareLink} size={180} bgColor="transparent" fgColor="#FFB300" includeMargin />
                <span className="text-xs text-white/60">{t('create.share.qr')}</span>
              </div>
            </div>
          </section>
        ) : null}

        <div className="flex items-center justify-between">
          <Button type="button" variant="ghost" onClick={() => goToStep(activeStep - 1)} disabled={activeStep === 0}>
            {t('form.prev')}
          </Button>
          {activeStep === stepFields.length - 1 ? (
            <Button type="submit" disabled={isSubmitting}>
              {t('form.finish')}
            </Button>
          ) : (
            <Button type="button" onClick={() => goToStep(activeStep + 1)}>
              {t('form.next')}
            </Button>
          )}
        </div>
      </div>
    </form>
  );
}
