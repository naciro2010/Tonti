import { createGlobalState } from '@vueuse/core';
import { nanoid } from '../utils/nanoid';
import type { ToastMessage } from '../types/toast';
import { ref } from 'vue';

export const useToast = createGlobalState(() => {
  const messages = ref<ToastMessage[]>([]);

  const remove = (id: string) => {
    messages.value = messages.value.filter((toast) => toast.id !== id);
  };

  const push = (toast: Omit<ToastMessage, 'id'>) => {
    const id = nanoid();
    messages.value = [...messages.value, { ...toast, id }];
    if (toast.duration !== Infinity && typeof window !== 'undefined') {
      const timeout = window.setTimeout(() => remove(id), toast.duration ?? 5000);
      return { id, timeout };
    }
    return { id };
  };

  return { messages, push, remove };
});
