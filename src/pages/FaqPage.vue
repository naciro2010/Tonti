<script setup lang="ts">
import { Disclosure, DisclosureButton, DisclosurePanel } from '@headlessui/vue';
import Tabs from '../components/ui/Tabs.vue';
import GlassCard from '../components/ui/GlassCard.vue';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';

const { t, tm } = useI18n();

const tabs = [
  { id: 'organizers', label: 'Organisateurs' },
  { id: 'contributors', label: 'Contributeurs' },
];

const active = ref('organizers');

const organizerFaq = computed(() => tm('faq.items') as { question: string; answer: string }[]);
const contributorFaq = computed(() => [
  {
    question: 'Comment contribuer ?',
    answer: 'Choisissez un montant pré-rempli ou personnalisez-le puis validez via la fenêtre de paiement.',
  },
  {
    question: 'Puis-je contribuer anonymement ?',
    answer: 'Oui, cochez la case “Contribution anonyme” avant de payer.',
  },
]);
</script>

<template>
  <section class="mx-auto max-w-4xl space-y-10 px-4 py-16">
    <header class="space-y-3 text-center">
      <h1 class="text-3xl font-semibold">{{ t('faq.title') }}</h1>
      <p class="text-white/70">Toutes les réponses pour lancer votre collecte en toute confiance.</p>
    </header>

    <Tabs :tabs="tabs" v-model:active="active">
      <template #default="{ active }">
        <div class="space-y-4">
          <GlassCard v-if="active === 'organizers'">
            <ul class="space-y-4">
              <li v-for="item in organizerFaq" :key="item.question">
                <Disclosure>
                  <DisclosureButton class="flex w-full items-center justify-between text-left text-base font-semibold">
                    <span>{{ item.question }}</span>
                    <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M6 8l4 4 4-4" />
                    </svg>
                  </DisclosureButton>
                  <DisclosurePanel class="mt-2 text-sm text-white/70">
                    {{ item.answer }}
                  </DisclosurePanel>
                </Disclosure>
              </li>
            </ul>
          </GlassCard>

          <GlassCard v-else>
            <ul class="space-y-4">
              <li v-for="item in contributorFaq" :key="item.question">
                <Disclosure>
                  <DisclosureButton class="flex w-full items-center justify-between text-left text-base font-semibold">
                    <span>{{ item.question }}</span>
                    <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M6 8l4 4 4-4" />
                    </svg>
                  </DisclosureButton>
                  <DisclosurePanel class="mt-2 text-sm text-white/70">
                    {{ item.answer }}
                  </DisclosurePanel>
                </Disclosure>
              </li>
            </ul>
          </GlassCard>
        </div>
      </template>
    </Tabs>
  </section>
</template>
