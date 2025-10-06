import type { ReactNode } from 'react';
import { twMerge } from 'tailwind-merge';

type CardProps = {
  title?: string;
  description?: string;
  children?: ReactNode;
  className?: string;
  footer?: ReactNode;
};

export function Card({ title, description, children, className, footer }: CardProps) {
  return (
    <article className={twMerge('flex flex-col rounded-3xl border border-white/10 bg-white/[0.04] p-6 shadow-lg shadow-primary/5', className)}>
      {title ? <h3 className="text-lg font-semibold text-white">{title}</h3> : null}
      {description ? <p className="mt-1 text-sm text-white/70">{description}</p> : null}
      {children ? <div className="mt-4 space-y-4">{children}</div> : null}
      {footer ? <div className="mt-6 border-t border-white/10 pt-4">{footer}</div> : null}
    </article>
  );
}
