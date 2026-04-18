<script setup lang="ts">
import { ref } from 'vue';
import QrcodeVue from 'qrcode.vue';
import { useI18n } from 'vue-i18n';

import BaseButton from './BaseButton.vue';

const props = defineProps<{
  invitationUrl: string;
}>();

const { t } = useI18n();
const copied = ref(false);

async function copy() {
  try {
    await navigator.clipboard.writeText(props.invitationUrl);
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 2500);
  } catch (error) {
    console.warn('Copy failed', error);
  }
}

async function share() {
  if (typeof navigator !== 'undefined' && 'share' in navigator) {
    try {
      await navigator.share({
        title: 'Tonti · Rejoindre un Daret',
        url: props.invitationUrl,
      });
      return;
    } catch (error) {
      /* user cancelled */
    }
  }
  copy();
}

const canShare =
  typeof navigator !== 'undefined' && 'share' in navigator;
</script>

<template>
  <section class="card relative overflow-hidden">
    <div
      class="pointer-events-none absolute inset-y-0 right-0 w-1/2 bg-gradient-to-l from-primary/10 via-primary/5 to-transparent"
      aria-hidden="true"
    />
    <div class="relative grid gap-6 sm:grid-cols-[1fr_auto] sm:items-center">
      <div class="space-y-4">
        <div class="space-y-1.5">
          <h3 class="flex items-center gap-2 text-lg font-semibold">
            <svg class="h-5 w-5 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M7.217 10.907a2.25 2.25 0 100 2.186m0-2.186c.18.324.283.696.283 1.093s-.103.77-.283 1.093m0-2.186l9.566-5.314m-9.566 7.5l9.566 5.314m0 0a2.25 2.25 0 103.935 2.186 2.25 2.25 0 00-3.935-2.186zm0-12.814a2.25 2.25 0 103.933-2.185 2.25 2.25 0 00-3.933 2.185z" />
            </svg>
            {{ t('invite.title') }}
          </h3>
          <p class="text-sm text-white/70">{{ t('invite.share') }}</p>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <div class="flex flex-1 min-w-0 items-center gap-2 rounded-xl border border-white/10 bg-surface/60 px-3 py-2">
            <svg class="h-4 w-4 flex-shrink-0 text-white/50" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
            </svg>
            <span class="truncate text-sm text-white/80">{{ props.invitationUrl }}</span>
          </div>
          <BaseButton type="button" size="sm" :variant="copied ? 'primary' : 'secondary'" @click="copy">
            <svg
              v-if="copied"
              class="h-4 w-4"
              viewBox="0 0 20 20"
              fill="currentColor"
              aria-hidden="true"
            >
              <path
                fill-rule="evenodd"
                d="M16.704 5.29a1 1 0 010 1.42l-8 8a1 1 0 01-1.415 0l-4-4a1 1 0 111.415-1.415L8 12.585l7.29-7.295a1 1 0 011.414 0z"
                clip-rule="evenodd"
              />
            </svg>
            <svg
              v-else
              class="h-4 w-4"
              viewBox="0 0 20 20"
              fill="currentColor"
              aria-hidden="true"
            >
              <path d="M8 2a1 1 0 011-1h4a1 1 0 011 1v1h2a2 2 0 012 2v10a2 2 0 01-2 2H6a2 2 0 01-2-2V5a2 2 0 012-2h2V2zm2 0v1h4V2h-4z" />
            </svg>
            {{ copied ? t('invite.copied') : t('actions.copyLink') }}
          </BaseButton>
          <BaseButton v-if="canShare" type="button" size="sm" variant="ghost" @click="share">
            <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path d="M15 8a3 3 0 10-2.977-2.63l-4.94 2.47a3 3 0 100 4.319l4.94 2.47a3 3 0 10.895-1.789l-4.94-2.47a3.027 3.027 0 000-.74l4.94-2.47C13.456 7.68 14.19 8 15 8z" />
            </svg>
            Partager
          </BaseButton>
        </div>
      </div>
      <div class="flex justify-center sm:justify-end">
        <div class="rounded-2xl bg-white p-3 shadow-lg">
          <QrcodeVue
            :value="props.invitationUrl"
            :size="120"
            level="M"
            :aria-label="t('invite.qrAlt')"
          />
        </div>
      </div>
    </div>
  </section>
</template>
