<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { UserRole } from '@/types';
import { useAuth } from '@/composables/useAuth';

const props = defineProps<{
  daretId: string;
  requiredRole?: UserRole;
  requireEdit?: boolean;
}>();

const { t } = useI18n();
const auth = useAuth();

const session = computed(() => auth.getSession(props.daretId));
const hasAccess = computed(() => {
  if (!session.value) return false;

  if (props.requiredRole) {
    return auth.hasRole(props.daretId, props.requiredRole);
  }

  if (props.requireEdit) {
    return auth.canEdit(props.daretId);
  }

  return true;
});
</script>

<template>
  <div v-if="!session">
    <div class="rounded-xl border border-warning/20 bg-warning/10 p-6 text-center">
      <div class="mb-4 text-4xl">🔒</div>
      <h3 class="mb-2 text-xl font-semibold">{{ t('auth.loginRequired') }}</h3>
      <p class="text-sm text-white/70">{{ t('auth.loginRequiredDesc') }}</p>
    </div>
  </div>

  <div v-else-if="!hasAccess">
    <div class="rounded-xl border border-danger/20 bg-danger/10 p-6 text-center">
      <div class="mb-4 text-4xl">⛔</div>
      <h3 class="mb-2 text-xl font-semibold">{{ t('auth.accessDenied') }}</h3>
      <p class="text-sm text-white/70">{{ t('auth.accessDeniedDesc') }}</p>
      <p class="mt-2 text-xs text-white/60">
        {{ t('auth.yourRole') }}: {{ session.role }}
      </p>
    </div>
  </div>

  <slot v-else />
</template>
