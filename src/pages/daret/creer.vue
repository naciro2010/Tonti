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
  <div class="space-y-8">
    <Stepper :steps="steps" :current="currentStep" />
    <p class="text-sm text-white/60">{{ t('wizard.autosave') }}</p>

    <form class="space-y-6" @submit.prevent="createDaret">
      <section v-if="currentStep === 0" class="card space-y-4">
        <BaseInput id="nom" v-model="state.values.nom" :label="t('form.name')" :error="state.errors.nom" required />
        <BaseInput id="description" v-model="state.values.description" :label="t('form.description')" />
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
              { label: 'MAD', value: 'MAD' },
              { label: 'EUR', value: 'EUR' },
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

      <section v-if="currentStep === 1" class="space-y-4">
        <RosterEditor v-model="state.values.membres" :seed="seedStorage" @update:seed="(seed) => (seedStorage.value = seed)" />
        <p v-if="!state.values.membres.length" class="text-sm text-white/60">{{ t('wizard.emptyRoster') }}</p>
        <p v-if="state.errors.roster" class="text-sm text-danger">{{ state.errors.roster }}</p>
      </section>

      <section v-if="currentStep === 2" class="card space-y-4">
        <BaseInput
          id="delai"
          v-model.number="state.values.regles.delaiGraceJours"
          type="number"
          :label="t('form.gracePeriod')"
          min="0"
          max="30"
        />
        <label class="flex items-center gap-2 text-sm">
          <input v-model="state.values.regles.rappelLocal" type="checkbox" />
          <span>{{ t('form.localReminders') }}</span>
        </label>
      </section>

      <section v-if="currentStep === 3" class="card space-y-3">
        <h2 class="text-xl font-semibold">{{ t('wizard.summary.title') }}</h2>
        <p class="text-sm text-white/60">{{ t('wizard.review') }}</p>
        <ul class="space-y-2 text-sm">
          <li><strong>{{ t('form.name') }}:</strong> {{ state.values.nom }}</li>
          <li><strong>{{ t('form.amount') }}:</strong> {{ state.values.montantMensuel }} {{ state.values.devise }}</li>
          <li><strong>{{ t('form.size') }}:</strong> {{ state.values.taille }}</li>
          <li><strong>{{ t('form.startDate') }}:</strong> {{ state.values.dateDebut || '—' }}</li>
          <li><strong>{{ t('form.visibility') }}:</strong> {{ state.values.visibilite }}</li>
          <li><strong>{{ t('wizard.step2') }}:</strong> {{ state.values.membres.length }} membres</li>
        </ul>
      </section>

      <div class="flex flex-wrap justify-between gap-3">
        <BaseButton type="button" variant="ghost" @click="previousStep" :disabled="currentStep === 0">
          {{ t('actions.previous') }}
        </BaseButton>
        <BaseButton
          v-if="currentStep < steps.length - 1"
          type="button"
          @click="nextStep"
        >
          {{ t('actions.next') }}
        </BaseButton>
        <BaseButton v-else type="submit">{{ t('actions.create') }}</BaseButton>
      </div>
    </form>

    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </div>
</template>
