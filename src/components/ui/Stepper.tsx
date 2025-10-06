import { twMerge } from 'tailwind-merge';

type Step = {
  id: number;
  title: string;
  description?: string;
};

type StepperProps = {
  steps: Step[];
  activeStep: number;
  onStepChange?: (step: number) => void;
};

export function Stepper({ steps, activeStep, onStepChange }: StepperProps) {
  return (
    <nav aria-label="Progress" className="flex flex-col space-y-4">
      {steps.map((step, index) => {
        const status =
          index < activeStep ? 'complete' : index === activeStep ? 'current' : 'upcoming';
        return (
          <button
            key={step.id}
            type="button"
            onClick={() => onStepChange?.(index)}
            className={twMerge(
              'rounded-2xl border border-white/10 bg-white/[0.03] px-4 py-3 text-left transition hover:border-primary/50 focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary',
              status === 'current' ? 'border-primary/70 bg-primary/10' : undefined,
            )}
          >
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-white/80">{step.title}</span>
              <span
                className={twMerge(
                  'flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold',
                  status === 'complete'
                    ? 'bg-primary text-secondary'
                    : status === 'current'
                      ? 'border border-primary/60 text-primary'
                      : 'border border-white/20 text-white/60',
                )}
              >
                {step.id}
              </span>
            </div>
            {step.description ? (
              <p className="mt-1 text-xs text-white/60">{step.description}</p>
            ) : null}
          </button>
        );
      })}
    </nav>
  );
}
