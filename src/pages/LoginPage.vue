<script setup lang="ts">
import { onMounted } from 'vue';
import { useLoginModal } from '../composables/useLoginModal';

const { open } = useLoginModal();

onMounted(() => {
  open();
});
</script>

<template>
  <section class="mx-auto max-w-3xl space-y-4 px-4 py-20 text-center">
    <h1 class="text-3xl font-semibold">Connexion</h1>
    <p class="text-white/70">La fenêtre de connexion va s’ouvrir automatiquement.</p>
  </section>
</template>
