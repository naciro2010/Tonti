import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { forwardRef } from 'react';
import { twMerge } from 'tailwind-merge';

const baseStyles = 'inline-flex items-center justify-center rounded-lg px-4 py-2 text-sm font-semibold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:opacity-60 disabled:cursor-not-allowed';

const variants = {
  primary: 'bg-primary text-secondary hover:bg-primary/90',
  secondary: 'bg-secondary text-white hover:bg-secondary/90',
  ghost: 'bg-transparent text-white hover:bg-white/10',
};

type Variant = keyof typeof variants;

type ButtonProps = {
  variant?: Variant;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
} & ButtonHTMLAttributes<HTMLButtonElement>;

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ variant = 'primary', className, leftIcon, rightIcon, children, ...rest }, ref) => {
    return (
      <button ref={ref} className={twMerge(baseStyles, variants[variant], className)} {...rest}>
        {leftIcon ? <span className="mr-2 flex items-center" aria-hidden>{leftIcon}</span> : null}
        <span>{children}</span>
        {rightIcon ? <span className="ml-2 flex items-center" aria-hidden>{rightIcon}</span> : null}
      </button>
    );
  },
);

Button.displayName = 'Button';
