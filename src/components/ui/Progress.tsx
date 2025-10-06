import { twMerge } from 'tailwind-merge';

type ProgressProps = {
  value: number;
  max?: number;
  label?: string;
};

export function Progress({ value, max = 100, label }: ProgressProps) {
  const percentage = Math.min(100, Math.round((value / max) * 100));
  return (
    <div className="space-y-2">
      {label ? <div className="flex items-center justify-between text-xs text-white/70">{label}</div> : null}
      <div className="relative h-3 w-full overflow-hidden rounded-full bg-white/10">
        <div
          className={twMerge('absolute inset-y-0 left-0 bg-primary transition-all duration-500 ease-out')}
          style={{ width: `${percentage}%` }}
          aria-valuemin={0}
          aria-valuemax={max}
          aria-valuenow={value}
          role="progressbar"
        />
      </div>
      <span className="block text-right text-xs font-semibold text-primary">{percentage}%</span>
    </div>
  );
}
