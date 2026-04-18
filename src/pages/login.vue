<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/composables/useAuthStore'
import { useToast } from '@/composables/useToast'
import BaseInput from '@/components/BaseInput.vue'
import BaseButton from '@/components/BaseButton.vue'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

const email = ref('')
const password = ref('')
const errors = ref<Record<string, string>>({})
const submitting = ref(false)

function validate() {
  errors.value = {}
  if (!email.value.trim()) errors.value.email = 'L\'email est requis'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) errors.value.email = 'Email invalide'
  if (!password.value) errors.value.password = 'Le mot de passe est requis'
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  try {
    const response = await auth.login(email.value.trim(), password.value)
    if (response.success) {
      toast.success('Connexion reussie !')
      router.push('/mes-darets')
    } else {
      toast.error(response.message || 'Identifiants invalides')
    }
  } catch (e: any) {
    toast.error(e.message || 'Erreur de connexion')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-md py-12 animate-fade-in-up">
    <div class="card p-8">
      <div class="mb-8 text-center">
        <div class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/15 text-primary ring-1 ring-inset ring-primary/30">
          <svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.5 20.25c0-3.728 3.022-6.75 6.75-6.75h1.5c3.728 0 6.75 3.022 6.75 6.75" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold">{{ t('auth.login') }}</h1>
        <p class="mt-2 text-sm text-white/60">Connectez-vous pour acceder a vos Darets</p>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-5">
        <BaseInput
          id="email"
          :label="'Email'"
          type="email"
          v-model="email"
          :error="errors.email"
          required
          autocomplete="email"
          placeholder="you@example.com"
        />

        <BaseInput
          id="password"
          :label="'Mot de passe'"
          type="password"
          v-model="password"
          :error="errors.password"
          required
          autocomplete="current-password"
          placeholder="********"
        />

        <BaseButton type="submit" variant="primary" block :loading="submitting">
          {{ submitting ? 'Connexion...' : t('auth.login') }}
        </BaseButton>
      </form>

      <p class="mt-6 text-center text-sm text-white/60">
        Pas encore de compte ?
        <RouterLink to="/inscription" class="font-semibold text-primary no-underline transition-colors hover:text-primaryHover">
          Creer un compte
        </RouterLink>
      </p>
    </div>
  </div>
</template>
