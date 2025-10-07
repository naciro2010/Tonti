export type ToastLevel = 'success' | 'info' | 'warning' | 'error';

export interface ToastMessage {
  id: string;
  title: string;
  description?: string;
  level: ToastLevel;
  duration?: number;
}
