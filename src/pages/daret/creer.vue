<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { useStorage } from '@vueuse/core';

import BaseButton from '@/components/BaseButton.vue';
import BaseInput from '@/components/BaseInput.vue';
import BaseSelect from '@/components/BaseSelect.vue';
import RosterEditor from '@/components/RosterEditor.vue';
import Stepper from '@/components/Stepper.vue';
import Toast from '@/components/Toast.vue';
import { useDaretStore, daretCreationSchema } from '@/composables/useDaretStore';
import { useZodForm } from '@/composables/useZodForm';

const { t } = useI18n();
const router = useRouter();
const store = useDaretStore();

const defaultForm = {
  nom: '',
  description: '',
  devise: 'MAD',
  montantMensuel: 100,
  taille: 10,
  createurId: 'organisateur-demo',
  membres: [],
  roster: [],
  visibilite: 'privee',
  dateDebut: '',
  regles: {
    delaiGraceJours: 3,
    rappelLocal: true,
  },
};

const storage = useStorage('tonti:wizard', defaultForm, undefined, { mergeDefaults: true });
const seedStorage = useStorage('tonti:wizard-seed', new Date().getFullYear().toString());

const { state, validate, submit, reset } = useZodForm(daretCreationSchema, storage.value ?? defaultForm);

watch(
  () => state.values,
  (value) => {
    storage.value = structuredClone(value);
  },
  { deep: true },
);

watch(
  () => state.values.membres,
  (members) => {
    state.values.roster = members.map((member) => member.id);
  },
  { deep: true },
);

const steps = computed(() => [t('wizard.step1'), t('wizard.step2'), t('wizard.step3'), t('wizard.step4')]);
const currentStep = ref(0);

const toast = reactive({ show: false, message: '', type: 'success' as 'success' | 'error' | 'info' });

function nextStep() {
  if (currentStep.value < steps.value.length - 1) {
    currentStep.value += 1;
  }
}

function previousStep() {
  if (currentStep.value > 0) {
    currentStep.value -= 1;
  }
}

async function createDaret() {
  const ok = validate();
  if (!ok) {
    toast.show = true;
    toast.message = t('wizard.validation');
    toast.type = 'error';
    setTimeout(() => (toast.show = false), 2500);
    return;
  }

  const success = await submit(async (values) => {
    const normalized = {
      ...values,
      montantMensuel: Number(values.montantMensuel),
      taille: Number(values.taille),
      regles: {
        ...values.regles,
        delaiGraceJours: Number(values.regles.delaiGraceJours),
      },
      dateDebut: values.dateDebut || undefined,
    };
    const daret = store.createDaret(normalized);
    toast.show = true;
    toast.message = t('wizard.success');
    toast.type = 'success';
    storage.value = structuredClone(defaultForm);
    reset(defaultForm);
    seedStorage.value = new Date().getFullYear().toString();
    await router.push(`/daret/${daret.id}`);
  });

  if (!success) {
    toast.show = true;
    toast.message = t('wizard.error');
    toast.type = 'error';
  }
}
</script>

<template>
  <div class="mx-auto max-w-4xl space-y-8">
    <header class="space-y-2">
      <h1 class="text-3xl font-bold tracking-tight sm:text-4xl">{{ t('wizard.summary.title') }}</h1>
      <p class="text-sm text-white/60">{{ t('wizard.review') }}</p>
    </header>

    <div class="card-interactive">
      <Stepper :steps="steps" :current="currentStep" />
      <p class="mt-4 inline-flex items-center gap-2 text-xs text-white/50">
        <svg class="h-3.5 w-3.5 text-primary" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
        </svg>
        {{ t('wizard.autosave') }}
      </p>
    </div>

    <form class="space-y-6" @submit.prevent="createDaret">
      <Transition
        enter-active-class="transition duration-300 ease-out"
        enter-from-class="opacity-0 translate-y-2"
        enter-to-class="opacity-100 translate-y-0"
        mode="out-in"
      >
        <section v-if="currentStep === 0" key="step-0" class="card space-y-5">
          <div>
            <h2 class="text-xl font-semibold">{{ t('wizard.step1') }}</h2>
            <p class="mt-1 text-sm text-white/60">Nom, montant, devise et dates clés de votre Daret.</p>
          </div>
          <BaseInput id="nom" v-model="state.values.nom" :label="t('form.name')" :error="state.errors.nom" required placeholder="Ex. Daret famille Casa" />
          <BaseInput id="description" v-model="state.values.description" :label="t('form.description')" placeholder="Quelques mots pour présenter cette Daret" />
          <div class="grid gap-4 sm:grid-cols-2">
            <BaseInput
              id="montant"
              v-model.number="state.values.montantMensuel"
              type="number"
              :label="t('form.amount')"
              min="10"
              :error="state.errors.montantMensuel"
            />
            <BaseInput
              id="taille"
              v-model.number="state.values.taille"
              type="number"
              :label="t('form.size')"
              min="2"
              max="50"
              :error="state.errors.taille"
            />
            <BaseSelect
              id="devise"
              v-model="state.values.devise"
              :label="t('form.currency')"
              :options="[
                { label: 'MAD (DH)', value: 'MAD' },
                { label: 'EUR (€)', value: 'EUR' },
              ]"
            />
            <BaseInput
              id="dateDebut"
              v-model="state.values.dateDebut"
              type="date"
              :label="t('form.startDate')"
              :error="state.errors.dateDebut"
            />
          </div>
          <BaseSelect
            id="visibilite"
            v-model="state.values.visibilite"
            :label="t('form.visibility')"
            :options="[
              { label: t('form.visibilityOptions.privee'), value: 'privee' },
              { label: t('form.visibilityOptions.non-listee'), value: 'non-listee' },
              { label: t('form.visibilityOptions.publique'), value: 'publique' },
            ]"
          />
        </section>

        <section v-else-if="currentStep === 1" key="step-1" class="space-y-4">
          <div class="card space-y-1.5">
            <h2 class="text-xl font-semibold">{{ t('wizard.step2') }}</h2>
            <p class="text-sm text-white/60">Ajoutez les membres et réglez l'ordre de tirage.</p>
          </div>
          <RosterEditor v-model="state.values.membres" :seed="seedStorage" @update:seed="(seed) => (seedStorage.value = seed)" />
          <p v-if="!state.values.membres.length" class="text-sm text-white/60">{{ t('wizard.emptyRoster') }}</p>
          <p v-if="state.errors.roster" class="text-sm text-dangerSoft">{{ state.errors.roster }}</p>
        </section>

        <section v-else-if="currentStep === 2" key="step-2" class="card space-y-6">
          <div>
            <h2 class="text-xl font-semibold">{{ t('wizard.step3') }}</h2>
            <p class="mt-1 text-sm text-white/60">Délai de grâce et rappels de paiement.</p>
          </div>
          <BaseInput
            id="delai"
            v-model.number="state.values.regles.delaiGraceJours"
            type="number"
            :label="t('form.gracePeriod')"
            min="0"
            max="30"
            hint="Nombre de jours tolérés après l'échéance avant qu'un paiement soit noté en retard."
          />
          <label
            class="flex cursor-pointer items-start gap-3 rounded-xl border border-white/10 bg-surface/50 p-4 transition-colors hover:border-primary/30 hover:bg-surface/70"
            :class="state.values.regles.rappelLocal ? 'border-primary/40 bg-primary/5' : ''"
          >
            <input
              v-model="state.values.regles.rappelLocal"
              type="checkbox"
              class="mt-0.5 h-4 w-4 flex-shrink-0 rounded border-white/20 bg-surface text-primary focus:ring-2 focus:ring-primary/40"
            />
            <div>
              <span class="block text-sm font-semibold text-white">{{ t('form.localReminders') }}</span>
              <span class="mt-0.5 block text-xs text-white/60">
                Reçoit des notifications navigateur le jour de l'échéance (avec votre accord).
              </span>
            </div>
          </label>
        </section>

        <section v-else-if="currentStep === 3" key="step-3" class="card space-y-5">
          <div>
            <h2 class="text-xl font-semibold">{{ t('wizard.step4') }}</h2>
            <p class="mt-1 text-sm text-white/60">Vérifiez les informations avant de créer votre Daret.</p>
          </div>
          <dl class="grid gap-3 sm:grid-cols-2">
            <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-white/50">{{ t('form.name') }}</dt>
              <dd class="mt-1 truncate font-semibold text-white">{{ state.values.nom || '—' }}</dd>
            </div>
            <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-white/50">{{ t('form.amount') }}</dt>
              <dd class="mt-1 font-semibold text-primary">
                {{ state.values.montantMensuel }} {{ state.values.devise }}
              </dd>
            </div>
            <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-white/50">{{ t('form.size') }}</dt>
              <dd class="mt-1 font-semibold text-white">{{ state.values.taille }} membres</dd>
            </div>
            <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-white/50">{{ t('form.startDate') }}</dt>
              <dd class="mt-1 font-semibold text-white">{{ state.values.dateDebut || '—' }}</dd>
            </div>
            <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-white/50">{{ t('form.visibility') }}</dt>
              <dd class="mt-1 font-semibold capitalize text-white">{{ state.values.visibilite }}</dd>
            </div>
            <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
              <dt class="text-xs font-semibold uppercase tracking-wider text-white/50">Roster</dt>
              <dd class="mt-1 font-semibold text-white">{{ state.values.membres.length }} membres</dd>
            </div>
          </dl>
        </section>
      </Transition>

      <div class="flex flex-col-reverse items-stretch justify-between gap-3 sm:flex-row sm:items-center">
        <BaseButton type="button" variant="ghost" @click="previousStep" :disabled="currentStep === 0">
          <svg class="h-4 w-4 rtl:rotate-180" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd" />
          </svg>
          {{ t('actions.previous') }}
        </BaseButton>
        <BaseButton
          v-if="currentStep < steps.length - 1"
          type="button"
          @click="nextStep"
        >
          {{ t('actions.next') }}
          <svg class="h-4 w-4 rtl:rotate-180" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
          </svg>
        </BaseButton>
        <BaseButton v-else type="submit" size="lg">
          <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          {{ t('actions.create') }}
        </BaseButton>
      </div>
    </form>

    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>
