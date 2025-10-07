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
</script>

<template>
  <section class="card grid gap-4 sm:grid-cols-[1fr_auto] sm:items-center">
    <div class="space-y-2">
      <h3 class="text-lg font-semibold">{{ t('invite.title') }}</h3>
      <p class="text-sm text-white/70">{{ t('invite.share') }}</p>
      <div class="flex flex-wrap items-center gap-3">
        <span class="truncate rounded-lg bg-white/10 px-3 py-2 text-sm">{{ props.invitationUrl }}</span>
        <BaseButton type="button" size="sm" @click="copy">{{ copied ? t('invite.copied') : t('actions.copyLink') }}</BaseButton>
      </div>
    </div>
    <QrcodeVue
      :value="props.invitationUrl"
      :size="120"
      class="mx-auto rounded-xl bg-white p-2"
      :aria-label="t('invite.qrAlt')"
    />
  </section>
</template>
