import { readonly, ref } from 'vue';

import { notificationApi } from '@/services/api';

const unreadCount = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

async function refresh() {
  try {
    unreadCount.value = await notificationApi.unreadCount();
  } catch {
    // Silencieux : le compteur n'est qu'indicatif
  }
}

function onVisibilityChange() {
  if (document.visibilityState === 'visible') void refresh();
}

export function useNotifications() {
  function startPolling(intervalMs = 60_000) {
    if (timer) return;
    void refresh();
    timer = setInterval(() => {
      if (document.visibilityState === 'visible') void refresh();
    }, intervalMs);
    document.addEventListener('visibilitychange', onVisibilityChange);
  }

  function stopPolling() {
    if (timer) clearInterval(timer);
    timer = null;
    document.removeEventListener('visibilitychange', onVisibilityChange);
  }

  function reset() {
    unreadCount.value = 0;
    stopPolling();
  }

  return { unreadCount: readonly(unreadCount), startPolling, stopPolling, reset, refresh };
}
