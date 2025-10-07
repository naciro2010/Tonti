<script setup lang="ts">
import { Dialog, DialogPanel, DialogTitle, TransitionChild, TransitionRoot } from '@headlessui/vue';

const props = defineProps<{
  modelValue: boolean;
  title: string;
}>();

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void;
}>();

function close() {
  emit('update:modelValue', false);
}
</script>

<template>
  <TransitionRoot :show="props.modelValue" as="template">
    <Dialog as="div" class="relative z-50" @close="close">
      <TransitionChild
        as="template"
        enter="ease-out duration-200"
        enter-from="opacity-0"
        enter-to="opacity-100"
        leave="ease-in duration-150"
        leave-from="opacity-100"
        leave-to="opacity-0"
      >
        <div class="fixed inset-0 bg-black/60" />
      </TransitionChild>

      <div class="fixed inset-0 overflow-y-auto">
        <div class="flex min-h-full items-center justify-center p-4">
          <TransitionChild
            as="template"
            enter="ease-out duration-200"
            enter-from="opacity-0 scale-95"
            enter-to="opacity-100 scale-100"
            leave="ease-in duration-150"
            leave-from="opacity-100 scale-100"
            leave-to="opacity-0 scale-95"
          >
            <DialogPanel class="w-full max-w-lg transform rounded-2xl bg-surface/80 p-6 text-left shadow-xl">
              <DialogTitle class="text-lg font-semibold text-white">{{ props.title }}</DialogTitle>
              <div class="mt-4 space-y-4">
                <slot />
              </div>
            </DialogPanel>
          </TransitionChild>
        </div>
      </div>
    </Dialog>
  </TransitionRoot>
</template>
