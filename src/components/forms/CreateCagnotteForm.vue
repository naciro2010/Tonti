<script setup lang="ts">
import { reactive, ref, computed, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { z } from 'zod';
import { useDebounceFn } from '@vueuse/core';
import Stepper, { type StepItem } from '../ui/Stepper.vue';
import TextInput from '../ui/TextInput.vue';
import TextArea from '../ui/TextArea.vue';
import SelectInput from '../ui/SelectInput.vue';
import BaseButton from '../ui/BaseButton.vue';
import GlassCard from '../ui/GlassCard.vue';
import { generateSlug } from '../../utils/slug';
import { safeStorage } from '../../utils/storage';
import { useToast } from '../../composables/useToast';
import { copyToClipboard, emailShareUrl, whatsappShareUrl } from '../../utils/share';
import { formatCurrency } from '../../utils/currency';
import { i18n } from '../../i18n';
import VueCropper from 'vue-cropperjs';
import 'cropperjs/dist/cropper.css';
import QrcodeVue from 'qrcode.vue';

const { t } = useI18n();
const { push } = useToast();

const steps = computed<StepItem[]>(() => [
  { id: 'description', label: t('create.step.description') },
  { id: 'illustration', label: t('create.step.illustration') },
  { id: 'visibility', label: t('create.step.visibility') },
  { id: 'amounts', label: t('create.step.amounts') },
  { id: 'invitations', label: t('create.step.invitations') },
]);

const current = ref('description');

const categories = [
  { value: 'urgence', label: 'Urgence' },
  { value: 'education', label: 'Éducation' },
  { value: 'sante', label: 'Santé' },
  { value: 'entrepreneuriat', label: 'Entrepreneuriat' },
  { value: 'sport', label: 'Sport' },
];

interface FormState {
  title: string;
  description: string;
  category: string;
  visibility: 'public' | 'private' | 'unlisted';
  goal: number;
  minimum: number;
  quickOptions: number[];
  slug: string;
  coverRaw: string | null;
  coverCropped: string | null;
}

const form = reactive<FormState>({
  title: '',
  description: '',
  category: '',
  visibility: 'public',
  goal: 5000,
  minimum: 20,
  quickOptions: [50, 100, 200],
  slug: '',
  coverRaw: null,
  coverCropped: null,
});

const errors = reactive<Record<string, string>>({});
const lockKey = 'tonti_form_draft_v1';

const descriptionSchema = z.object({
  title: z.string().min(4),
  description: z.string().min(20),
  category: z.string().min(1),
});

const illustrationSchema = z.object({
  coverCropped: z.string().min(1),
});

const visibilitySchema = z.object({
  visibility: z.enum(['public', 'private', 'unlisted']),
});

const amountsSchema = z.object({
  goal: z.number().min(100),
  minimum: z.number().min(10),
  quickOptions: z.array(z.number().min(10)).min(1),
});

const validators: Record<string, z.ZodSchema> = {
  description: descriptionSchema,
  illustration: illustrationSchema,
  visibility: visibilitySchema,
  amounts: amountsSchema,
  invitations: z.object({}),
};

const cropper = ref<InstanceType<typeof VueCropper> | null>(null);
const isSaving = ref(false);
const hasRestored = ref(false);
let skipNextSave = true;

const stepOrder = computed(() => steps.value.map((step) => step.id));

const saveDraft = useDebounceFn(() => {
  if (typeof window === 'undefined' || !hasRestored.value) return;
  safeStorage.set(lockKey, JSON.stringify(form));
  push({ level: 'success', title: t('create.actions.saved'), duration: 2500 });
}, 800);

watch(
  () => ({ ...form }),
  () => {
    if (skipNextSave) {
      skipNextSave = false;
      return;
    }
    isSaving.value = true;
    saveDraft();
    if (typeof window !== 'undefined') {
      window.setTimeout(() => {
        isSaving.value = false;
      }, 900);
    } else {
      isSaving.value = false;
    }
  },
  { deep: true }
);

watch(
  () => form.title,
  (title) => {
    form.slug = generateSlug(title);
  }
);

onMounted(() => {
  const stored = safeStorage.get(lockKey);
  if (stored) {
    try {
      const payload = JSON.parse(stored) as FormState;
      Object.assign(form, payload);
    } catch (error) {
      console.error('Unable to restore draft', error);
    }
  }
  hasRestored.value = true;
});

const validateCurrent = () => {
  const schema = validators[current.value];
  const result = schema.safeParse(form);
  Object.keys(errors).forEach((key) => delete errors[key]);
  if (!result.success) {
    result.error.issues.forEach((issue) => {
      const field = issue.path[0];
      errors[field as string] = issue.message;
    });
    return false;
  }
  return true;
};

const goTo = (id: string) => {
  const targetIndex = stepOrder.value.indexOf(id);
  const currentIndex = stepOrder.value.indexOf(current.value);
  if (targetIndex <= currentIndex) {
    current.value = id;
    return;
  }
  if (validateCurrent()) {
    current.value = id;
  }
};

const next = () => {
  if (!validateCurrent()) return;
  const index = stepOrder.value.indexOf(current.value);
  if (index < stepOrder.value.length - 1) {
    current.value = stepOrder.value[index + 1];
  }
};

const previous = () => {
  const index = stepOrder.value.indexOf(current.value);
  if (index > 0) {
    current.value = stepOrder.value[index - 1];
  }
};

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'ArrowRight') {
    event.preventDefault();
    next();
  }
  if (event.key === 'ArrowLeft') {
    event.preventDefault();
    previous();
  }
  if (event.key === 'Enter' && event.metaKey) {
    event.preventDefault();
    submit();
  }
};

const uploadCover = async (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = () => {
    form.coverRaw = reader.result as string;
  };
  reader.readAsDataURL(file);
};

const confirmCrop = () => {
  if (!cropper.value) return;
  const canvas = cropper.value.getCroppedCanvas({ aspectRatio: 16 / 9, width: 1200 });
  form.coverCropped = canvas.toDataURL('image/jpeg');
  push({ level: 'success', title: 'Image recadrée en 16:9' });
};

const addQuickOption = () => {
  const nextValue = Math.max(...form.quickOptions, form.minimum) + 50;
  form.quickOptions = [...form.quickOptions, nextValue];
};

const removeQuickOption = (amount: number) => {
  form.quickOptions = form.quickOptions.filter((value) => value !== amount);
};

const shareUrl = computed(() => `https://tonti.app/cagnotte/${form.slug || 'ma-nouvelle-cagnotte'}`);

const copyLink = async () => {
  await copyToClipboard(shareUrl.value);
  push({ level: 'success', title: t('create.invitations.copied') });
};

const shareWhatsapp = () => {
  if (typeof window !== 'undefined') {
    window.open(whatsappShareUrl(shareUrl.value), '_blank');
  }
};

const shareEmail = () => {
  if (typeof window !== 'undefined') {
    window.open(emailShareUrl('Cagnotte Tonti', shareUrl.value), '_blank');
  }
};

const submit = () => {
  if (!validateCurrent()) {
    push({ level: 'error', title: 'Veuillez corriger les erreurs' });
    return;
  }
  push({ level: 'success', title: 'Cagnotte prête à être publiée', description: 'Connectez votre futur backend de paiement.' });
  safeStorage.remove(lockKey);
};
</script>

<template>
  <div
    class="space-y-10"
    @keydown="handleKeydown"
    tabindex="0"
    role="form"
    aria-labelledby="create-title"
  >
    <Stepper :steps="steps" :current="current" @select="goTo" />

    <GlassCard class="space-y-6">
      <section v-if="current === 'description'" class="space-y-6">
        <header class="space-y-2">
          <h2 class="text-xl font-semibold">{{ t('create.step.description') }}</h2>
          <p class="text-sm text-white/60">{{ t('create.intro') }}</p>
        </header>
        <div class="grid gap-6">
          <TextInput
            id="title"
            v-model="form.title"
            :label="t('create.description.titleLabel')"
            :placeholder="t('create.description.titlePlaceholder')"
            :error="errors.title"
            required
          />
          <TextArea
            id="description"
            v-model="form.description"
            :label="t('create.description.descriptionLabel')"
            :placeholder="t('create.description.descriptionPlaceholder')"
            :error="errors.description"
            rows="6"
          />
          <SelectInput
            id="category"
            v-model="form.category"
            :label="t('create.description.categoryLabel')"
            :options="categories"
            :error="errors.category"
          />
        </div>
      </section>

      <section v-else-if="current === 'illustration'" class="space-y-6">
        <header class="space-y-2">
          <h2 class="text-xl font-semibold">{{ t('create.step.illustration') }}</h2>
          <p class="text-sm text-white/60">{{ t('create.illustration.helper') }}</p>
        </header>
        <div class="space-y-4">
          <input type="file" accept="image/*" @change="uploadCover" />
          <div v-if="form.coverRaw" class="space-y-4">
            <VueCropper
              ref="cropper"
              :src="form.coverRaw"
              drag-mode="move"
              :aspect-ratio="16 / 9"
              class="h-64 w-full rounded-2xl bg-black"
            />
            <BaseButton type="button" @click="confirmCrop">{{ t('create.illustration.changeButton') }}</BaseButton>
          </div>
          <img v-if="form.coverCropped" :src="form.coverCropped" alt="Preview" class="h-48 w-full rounded-2xl object-cover" />
          <p v-if="errors.coverCropped" class="text-sm text-red-400">{{ errors.coverCropped }}</p>
        </div>
      </section>

      <section v-else-if="current === 'visibility'" class="space-y-6">
        <header class="space-y-2">
          <h2 class="text-xl font-semibold">{{ t('create.step.visibility') }}</h2>
          <p class="text-sm text-white/60">{{ t('create.visibility.helper') }}</p>
        </header>
        <div class="grid gap-4 sm:grid-cols-3">
          <label class="flex cursor-pointer flex-col gap-3 rounded-2xl border border-white/10 bg-white/5 p-4 hover:border-primary">
            <input v-model="form.visibility" type="radio" value="public" class="sr-only" />
            <span class="text-lg font-semibold">{{ t('create.visibility.public') }}</span>
            <p class="text-sm text-white/60">Affichée dans les recherches Tonti.</p>
          </label>
          <label class="flex cursor-pointer flex-col gap-3 rounded-2xl border border-white/10 bg-white/5 p-4 hover:border-primary">
            <input v-model="form.visibility" type="radio" value="private" class="sr-only" />
            <span class="text-lg font-semibold">{{ t('create.visibility.private') }}</span>
            <p class="text-sm text-white/60">Accès limité aux invités approuvés.</p>
          </label>
          <label class="flex cursor-pointer flex-col gap-3 rounded-2xl border border-white/10 bg-white/5 p-4 hover:border-primary">
            <input v-model="form.visibility" type="radio" value="unlisted" class="sr-only" />
            <span class="text-lg font-semibold">{{ t('create.visibility.unlisted') }}</span>
            <p class="text-sm text-white/60">Accessible uniquement via le lien direct.</p>
          </label>
        </div>
        <p v-if="errors.visibility" class="text-sm text-red-400">{{ errors.visibility }}</p>
      </section>

      <section v-else-if="current === 'amounts'" class="space-y-6">
        <header class="space-y-2">
          <h2 class="text-xl font-semibold">{{ t('create.step.amounts') }}</h2>
          <p class="text-sm text-white/60">Définissez des objectifs réalistes pour motiver vos contributeurs.</p>
        </header>
        <div class="grid gap-6 md:grid-cols-2">
          <TextInput
            id="goal"
            v-model="form.goal"
            type="number"
            :label="t('create.amounts.goalLabel')"
            :error="errors.goal"
          />
          <TextInput
            id="minimum"
            v-model="form.minimum"
            type="number"
            :label="t('create.amounts.minContributionLabel')"
            :error="errors.minimum"
          />
        </div>
        <div class="space-y-3">
          <p class="text-sm font-semibold">{{ t('create.amounts.quickOptionsLabel') }}</p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="amount in form.quickOptions"
              :key="amount"
              type="button"
              class="rounded-full border border-white/20 px-4 py-2 text-sm text-white/80 hover:border-primary"
              @click="removeQuickOption(amount)"
            >
              {{ formatCurrency(amount, i18n.global.locale.value === 'ar' ? 'ar-MA' : 'fr-MA') }}
              <span class="ml-1 text-xs text-white/50">×</span>
            </button>
            <BaseButton variant="ghost" type="button" @click="addQuickOption">{{ t('create.amounts.addOption') }}</BaseButton>
          </div>
        </div>
        <p v-if="errors.quickOptions" class="text-sm text-red-400">{{ errors.quickOptions }}</p>
      </section>

      <section v-else-if="current === 'invitations'" class="space-y-6">
        <header class="space-y-2">
          <h2 class="text-xl font-semibold">{{ t('create.step.invitations') }}</h2>
          <p class="text-sm text-white/60">{{ t('create.invitations.shareDescription') }}</p>
        </header>
        <div class="grid gap-6 lg:grid-cols-2">
          <div class="space-y-4">
            <p class="text-sm text-white/70">Lien</p>
            <div class="rounded-2xl border border-white/10 bg-white/5 p-4">
              <p class="text-sm break-all text-white/80">{{ shareUrl }}</p>
              <div class="mt-3 flex flex-wrap gap-3">
                <BaseButton type="button" size="sm" @click="copyLink">{{ t('create.invitations.copyLink') }}</BaseButton>
                <BaseButton type="button" size="sm" variant="ghost" @click="shareWhatsapp">
                  {{ t('create.invitations.whatsapp') }}
                </BaseButton>
                <BaseButton type="button" size="sm" variant="ghost" @click="shareEmail">
                  {{ t('create.invitations.email') }}
                </BaseButton>
              </div>
            </div>
            <p class="text-xs text-white/50">{{ t('create.actions.saving') }} {{ isSaving ? '…' : '' }}</p>
          </div>
          <div class="space-y-4">
            <p class="text-sm text-white/70">{{ t('create.invitations.qrLabel') }}</p>
            <div class="inline-flex rounded-2xl border border-white/10 bg-white p-4">
              <QrcodeVue :value="shareUrl" :size="160" level="H" render-as="svg" />
            </div>
          </div>
        </div>
      </section>
    </GlassCard>

    <div class="flex flex-wrap items-center justify-between gap-4">
      <BaseButton variant="ghost" type="button" @click="previous">{{ t('create.actions.previous') }}</BaseButton>
      <div class="flex items-center gap-3">
        <span v-if="isSaving" class="text-sm text-white/60">{{ t('create.actions.saving') }}</span>
        <BaseButton v-if="current !== 'invitations'" type="button" @click="next">{{ t('create.actions.next') }}</BaseButton>
        <BaseButton v-else type="button" @click="submit">{{ t('create.actions.submit') }}</BaseButton>
      </div>
    </div>
  </div>
</template>
