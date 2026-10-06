<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import { formatDateTime } from '@/composables/useDates';
import { useNotifications } from '@/composables/useNotifications';
import { notificationApi, type NotificationResponse } from '@/services/api';
import { errorMessage } from '@/utils/errors';

const { t, locale } = useI18n();
const router = useRouter();
const unread = useNotifications();

const items = ref<NotificationResponse[]>([]);
const loading = ref(true);
const loadError = ref<string | null>(null);

async function load() {
  loading.value = true;
  loadError.value = null;
  try {
    items.value = (await notificationApi.list()).content;
  } catch (error) {
    loadError.value = errorMessage(error, t('common.error'));
  } finally {
    loading.value = false;
  }
}

async function markAll() {
  await notificationApi.markAllAsRead().catch(() => undefined);
  items.value = items.value.map((item) => ({ ...item, isRead: true }));
  void unread.refresh();
}

function daretIdOf(item: NotificationResponse): string | null {
  if (!item.data) return null;
  try {
    const data = JSON.parse(item.data) as { daretId?: string };
    return data.daretId ?? null;
  } catch {
    return null;
  }
}

async function open(item: NotificationResponse) {
  if (!item.isRead) {
    item.isRead = true;
    void notificationApi
      .markAsRead(item.id)
      .then(() => unread.refresh())
      .catch(() => undefined);
  }
  const daretId = daretIdOf(item);
  if (daretId) await router.push(`/daret/${daretId}`);
}

onMounted(load);
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <header class="flex items-center justify-between gap-3">
      <h1 class="text-2xl font-bold sm:text-3xl">{{ t('notifications.title') }}</h1>
      <BaseButton v-if="items.some((item) => !item.isRead)" size="sm" variant="ghost" @click="markAll">
        {{ t('notifications.markAll') }}
      </BaseButton>
    </header>

    <div v-if="loading" class="space-y-3" aria-busy="true">
      <div v-for="i in 5" :key="i" class="skeleton h-20 rounded-2xl" />
    </div>

    <div v-else-if="loadError" class="card text-center">
      <p class="text-white/70">{{ loadError }}</p>
      <BaseButton class="mt-4" variant="secondary" @click="load">{{ t('common.retry') }}</BaseButton>
    </div>

    <p v-else-if="items.length === 0" class="card text-center text-white/60">
      {{ t('notifications.empty') }}
    </p>

    <ul v-else class="space-y-2">
      <li v-for="item in items" :key="item.id">
        <button
          type="button"
          class="flex w-full items-start gap-3 rounded-2xl border p-4 text-start transition-colors"
          :class="item.isRead ? 'border-white/5 bg-surface/30' : 'border-primary/30 bg-primary/5'"
          @click="open(item)"
        >
          <span
            class="mt-1.5 h-2 w-2 flex-shrink-0 rounded-full"
            :class="item.isRead ? 'bg-transparent' : 'bg-primary'"
            aria-hidden="true"
          />
          <span class="min-w-0 flex-1">
            <span class="block font-semibold">{{ item.title }}</span>
            <span class="mt-0.5 block text-sm text-white/70">{{ item.message }}</span>
            <span class="mt-1 block text-xs text-white/40">{{ formatDateTime(item.createdAt, locale) }}</span>
          </span>
        </button>
      </li>
    </ul>
  </div>
</template>
