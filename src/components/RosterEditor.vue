<script setup lang="ts">
import { ref, watch } from 'vue';
import { nanoid } from 'nanoid';

import type { Membre } from '@/types';
import { randomizeRoster } from '@/utils/roster';

import BaseButton from './BaseButton.vue';
import BaseInput from './BaseInput.vue';

const members = defineModel<Membre[]>({ default: [] });

const props = withDefaults(
  defineProps<{
    seed?: string;
  }>(),
  {
    seed: undefined,
  },
);

const emit = defineEmits<{
  (event: 'update:seed', value: string): void;
}>();

const form = ref({
  nom: '',
  contact: '',
});

const seedValue = ref(props.seed ?? new Date().getFullYear().toString());

watch(
  () => props.seed,
  (value) => {
    if (value) {
      seedValue.value = value;
    }
  },
);

function addMember() {
  if (!form.value.nom.trim()) {
    return;
  }

  members.value = [
    ...members.value,
    {
      id: nanoid(6),
      nom: form.value.nom.trim(),
      contact: form.value.contact.trim() || undefined,
    },
  ];

  form.value = { nom: '', contact: '' };
}

let dragIndex: number | null = null;

function onDragStart(index: number) {
  dragIndex = index;
}

function onDrop(index: number) {
  if (dragIndex === null || dragIndex === index) {
    dragIndex = null;
    return;
  }

  const updated = [...members.value];
  const [moved] = updated.splice(dragIndex, 1);
  updated.splice(index, 0, moved);
  members.value = updated;
  dragIndex = null;
}

function removeMember(index: number) {
  members.value = members.value.filter((_, idx) => idx !== index);
}

function randomize() {
  const seed = seedValue.value.trim() || new Date().toISOString();
  const orderedIds = randomizeRoster(members.value, seed);
  const map = new Map(members.value.map((member) => [member.id, member]));
  members.value = orderedIds.map((id) => map.get(id)!).filter(Boolean);
  emit('update:seed', seed);
}
</script>

<template>
  <section class="space-y-4">
    <div class="grid gap-4 rounded-xl bg-white/5 p-4 sm:grid-cols-2">
      <BaseInput
        id="member-name"
        v-model="form.nom"
        :label="$t('form.memberName')"
        required
      />
      <BaseInput
        id="member-contact"
        v-model="form.contact"
        :label="$t('form.memberContact')"
      />
      <div class="sm:col-span-2 flex items-end justify-between gap-2">
        <BaseButton type="button" variant="secondary" @click="addMember">
          {{ $t('actions.addMember') }}
        </BaseButton>
        <div class="flex items-center gap-2">
          <BaseInput
            id="roster-seed"
            v-model="seedValue"
            :label="$t('wizard.seed')"
            class="w-32"
          />
          <BaseButton type="button" variant="ghost" @click="randomize">
            {{ $t('actions.randomize') }}
          </BaseButton>
        </div>
      </div>
    </div>

    <ul class="space-y-2">
      <li
        v-for="(member, index) in members"
        :key="member.id"
        draggable
        @dragstart="onDragStart(index)"
        @dragover.prevent
        @drop.prevent="onDrop(index)"
        class="flex items-center justify-between gap-4 rounded-xl border border-white/10 bg-surface/60 px-4 py-3"
      >
        <div class="flex items-center gap-3">
          <span class="text-sm text-white/60">{{ index + 1 }}</span>
          <div>
            <p class="font-semibold">{{ member.nom }}</p>
            <p v-if="member.contact" class="text-xs text-white/60">{{ member.contact }}</p>
          </div>
        </div>
        <BaseButton type="button" variant="ghost" @click="removeMember(index)">
          <span class="sr-only">{{ $t('actions.remove') }}</span>
          ✕
        </BaseButton>
      </li>
    </ul>
  </section>
</template>
