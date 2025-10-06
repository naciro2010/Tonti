import type { InputHTMLAttributes } from 'react';
import { forwardRef } from 'react';
import { twMerge } from 'tailwind-merge';

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
  error?: string;
  helper?: string;
};

const baseInput = 'w-full rounded-lg border border-white/20 bg-white/5 px-3 py-2 text-sm text-white placeholder:text-white/40 focus:border-primary focus:ring-primary';

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helper, id, className, ...props }, ref) => {
    const inputId = id ?? props.name ?? label?.toLowerCase().replace(/\s+/g, '-');

    return (
      <label className="flex w-full flex-col space-y-1 text-sm">
        {label ? <span className="font-medium text-white/90">{label}</span> : null}
        <input ref={ref} id={inputId} className={twMerge(baseInput, className, error ? 'border-red-500' : undefined)} {...props} />
        {helper && !error ? <span className="text-xs text-white/60">{helper}</span> : null}
        {error ? <span className="text-xs text-red-400">{error}</span> : null}
      </label>
    );
  },
);

Input.displayName = 'Input';
