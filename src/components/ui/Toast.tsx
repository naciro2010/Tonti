import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { twMerge } from 'tailwind-merge';

type ToastVariant = 'default' | 'success' | 'error';

type Toast = {
  id: number;
  title: string;
  description?: string;
  variant?: ToastVariant;
};

type ToastContextValue = {
  push: (toast: Omit<Toast, 'id'>) => void;
};

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

const variantStyles: Record<ToastVariant, string> = {
  default: 'border-white/20 bg-white/[0.04]',
  success: 'border-emerald-400/40 bg-emerald-500/10 text-emerald-200',
  error: 'border-red-400/40 bg-red-500/10 text-red-200',
};

let counter = 0;

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const push = useCallback((toast: Omit<Toast, 'id'>) => {
    const id = ++counter;
    setToasts((current) => [...current, { id, ...toast }]);
    setTimeout(() => {
      setToasts((current) => current.filter((t) => t.id !== id));
    }, 4000);
  }, []);

  const value = useMemo(() => ({ push }), [push]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-container">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            role="status"
            className={twMerge(
              'w-full max-w-sm rounded-2xl border px-4 py-3 text-sm text-white shadow-lg backdrop-blur-sm',
              variantStyles[toast.variant ?? 'default'],
            )}
          >
            <p className="font-semibold">{toast.title}</p>
            {toast.description ? <p className="text-xs text-white/70">{toast.description}</p> : null}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used inside a ToastProvider');
  }
  return context;
}
