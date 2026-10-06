<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import BaseInput from '@/components/BaseInput.vue';
import BaseSelect from '@/components/BaseSelect.vue';
import Stepper from '@/components/Stepper.vue';
import { formatCurrency } from '@/composables/useCurrency';
import { useToast } from '@/composables/useToast';
import { daretApi, type Currency, type Visibility } from '@/services/api';
import { errorMessage, fieldErrors } from '@/utils/errors';

const { t } = useI18n();
const router = useRouter();
const toast = useToast();

const form = reactive({
  nom: '',
  description: '',
  devise: 'MAD' as Currency,
  montantMensuel: 500 as number | string,
  taille: 6 as number | string,
  delaiGraceJours: 3 as number | string,
  visibilite: 'PRIVEE' as Visibility,
});

const step = ref(0);
const errors = ref<Record<string, string>>({});
const submitting = ref(false);

const steps = computed(() => [
  t('create.steps.info'),
  t('create.steps.contribution'),
  t('create.steps.summary'),
]);
const amount = computed(() => Number(form.montantMensuel));
const size = computed(() => Number(form.taille));
const pot = computed(() => amount.value * (size.value - 1));

const currencyOptions = computed(() => [
  { value: 'MAD', label: t('currency.MAD') },
  { value: 'EUR', label: t('currency.EUR') },
  { value: 'USD', label: t('currency.USD') },
]);
const visibilityOptions = computed(() => [
  { value: 'PRIVEE', label: t('create.visibility.PRIVEE') },
  { value: 'NON_LISTEE', label: t('create.visibility.NON_LISTEE') },
  { value: 'PUBLIQUE', label: t('create.visibility.PUBLIQUE') },
]);

function validateStep(index: number) {
  const e: Record<string, string> = {};
  if (index === 0) {
    if (form.nom.trim().length < 3) e.nom = t('create.errors.name');
    if (form.description.length > 1000) e.description = t('create.errors.description');
  }
  if (index === 1) {
    if (!Number.isFinite(amount.value) || amount.value < 10 || amount.value > 100000)
      e.montantMensuel = t('create.errors.amount');
    if (!Number.isInteger(size.value) || size.value < 2 || size.value > 50)
      e.taille = t('create.errors.size');
    const grace = Number(form.delaiGraceJours);
    if (!Number.isInteger(grace) || grace < 0 || grace > 30) e.delaiGraceJours = t('create.errors.grace');
  }
  errors.value = e;
  return Object.keys(e).length === 0;
}

function next() {
  if (validateStep(step.value)) step.value += 1;
}

async function submit() {
  if (!validateStep(0) || !validateStep(1)) return;
  submitting.value = true;
  try {
    const daret = await daretApi.create({
      nom: form.nom.trim(),
      description: form.description.trim() || undefined,
      devise: form.devise,
      montantMensuel: amount.value,
      taille: size.value,
      visibilite: form.visibilite,
      delaiGraceJours: Number(form.delaiGraceJours),
    });
    toast.success(t('create.success'));
    await router.replace(`/daret/${daret.id}`);
  } catch (error) {
    errors.value = fieldErrors(error);
    toast.error(errorMessage(error, t('common.error')));
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <header>
      <h1 class="text-2xl font-bold sm:text-3xl">{{ t('create.title') }}</h1>
      <p class="mt-1 text-sm text-white/60">{{ t('create.subtitle') }}</p>
    </header>

    <Stepper :steps="steps" :current="step" />

    <form class="card space-y-5" novalidate @submit.prevent="step < 2 ? next() : submit()">
      <template v-if="step === 0">
        <BaseInput
          id="nom"
          v-model="form.nom"
          :label="t('create.fields.name')"
          :placeholder="t('create.fields.namePlaceholder')"
          :error="errors.nom"
          maxlength="100"
          required
        />
        <div class="space-y-1.5">
          <label for="description">{{ t('create.fields.description') }} ({{ t('common.optional') }})</label>
          <textarea
            id="description"
            v-model="form.description"
            rows="3"
            maxlength="1000"
            :aria-invalid="errors.description ? 'true' : undefined"
          />
          <p v-if="errors.description" class="text-xs text-dangerSoft" role="alert">
            {{ errors.description }}
          </p>
        </div>
      </template>

      <template v-else-if="step === 1">
        <div class="grid gap-4 sm:grid-cols-2">
          <BaseInput
            id="montant"
            v-model="form.montantMensuel"
            :label="t('create.fields.amount')"
            type="number"
            inputmode="decimal"
            min="10"
            step="10"
            :error="errors.montantMensuel"
            required
          />
          <BaseSelect
            id="devise"
            v-model="form.devise"
            :label="t('create.fields.currency')"
            :options="currencyOptions"
          />
        </div>
        <BaseInput
          id="taille"
          v-model="form.taille"
          :label="t('create.fields.size')"
          :hint="t('create.fields.sizeHint')"
          type="number"
          inputmode="numeric"
          min="2"
          max="50"
          :error="errors.taille"
          required
        />
        <div class="grid gap-4 sm:grid-cols-2">
          <BaseInput
            id="grace"
            v-model="form.delaiGraceJours"
            :label="t('create.fields.grace')"
            :hint="t('create.fields.graceHint')"
            type="number"
            inputmode="numeric"
            min="0"
            max="30"
            :error="errors.delaiGraceJours"
          />
          <BaseSelect
            id="visibilite"
            v-model="form.visibilite"
            :label="t('create.fields.visibility')"
            :options="visibilityOptions"
          />
        </div>
      </template>

      <template v-else>
        <dl class="grid gap-3 sm:grid-cols-2">
          <div class="rounded-xl bg-white/5 p-4">
            <dt class="section-title">{{ t('create.fields.name') }}</dt>
            <dd class="mt-1 font-semibold">{{ form.nom }}</dd>
          </div>
          <div class="rounded-xl bg-white/5 p-4">
            <dt class="section-title">{{ t('create.fields.amount') }}</dt>
            <dd class="mt-1 font-semibold">
              {{ formatCurrency(amount, form.devise) }} / {{ t('common.month') }}
            </dd>
          </div>
          <div class="rounded-xl bg-white/5 p-4">
            <dt class="section-title">{{ t('create.fields.size') }}</dt>
            <dd class="mt-1 font-semibold">{{ t('create.summary.duration', { count: size }, size) }}</dd>
          </div>
          <div class="rounded-xl bg-primary/10 p-4 ring-1 ring-inset ring-primary/30">
            <dt class="section-title">{{ t('create.summary.pot') }}</dt>
            <dd class="mt-1 text-xl font-bold text-primary">{{ formatCurrency(pot, form.devise) }}</dd>
          </div>
        </dl>
        <p class="text-sm text-white/60">{{ t('create.summary.next') }}</p>
      </template>

      <div class="flex justify-between gap-3 pt-2">
        <BaseButton v-if="step > 0" variant="secondary" @click="step -= 1">{{ t('common.back') }}</BaseButton>
        <span v-else />
        <BaseButton type="submit" :loading="submitting">
          {{ step < 2 ? t('common.next') : t('create.submit') }}
        </BaseButton>
      </div>
    </form>
  </div>
</template>
