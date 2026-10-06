<script setup lang="ts">
import QrcodeVue from 'qrcode.vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

import BaseButton from './BaseButton.vue';

const props = defineProps<{
  code: string;
  invitationUrl: string;
}>();

const { t } = useI18n();
const copied = ref(false);
const canShare = typeof navigator !== 'undefined' && typeof navigator.share === 'function';

async function copy() {
  try {
    await navigator.clipboard.writeText(props.invitationUrl);
    copied.value = true;
    setTimeout(() => (copied.value = false), 2500);
  } catch {
    // Presse-papiers indisponible : l'utilisateur peut recopier le code affiché
  }
}

async function share() {
  try {
    await navigator.share({
      title: t('invite.shareTitle'),
      text: t('invite.shareText', { code: props.code }),
      url: props.invitationUrl,
    });
  } catch {
    // Partage annulé par l'utilisateur
  }
}
</script>

<template>
  <section class="card grid gap-6 sm:grid-cols-[1fr_auto] sm:items-center">
    <div class="space-y-4">
      <div>
        <h3>{{ t('invite.title') }}</h3>
        <p class="mt-1 text-sm text-white/60">{{ t('invite.subtitle') }}</p>
      </div>
      <p class="font-mono text-3xl font-bold tracking-[0.3em] text-primary" :aria-label="t('invite.code')">
        {{ code }}
      </p>
      <div class="flex flex-wrap gap-2">
        <BaseButton v-if="canShare" size="sm" @click="share">{{ t('common.share') }}</BaseButton>
        <BaseButton size="sm" variant="secondary" @click="copy">
          {{ copied ? t('common.copied') : t('invite.copyLink') }}
        </BaseButton>
      </div>
    </div>
    <div class="mx-auto rounded-2xl bg-white p-3">
      <QrcodeVue :value="invitationUrl" :size="128" level="M" render-as="svg" />
    </div>
  </section>
</template>
