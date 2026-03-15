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
  <div class="mx-auto max-w-md py-12">
    <div class="card p-8">
      <h1 class="mb-2 text-2xl font-bold">{{ t('auth.login') }}</h1>
      <p class="mb-8 text-sm text-white/60">Connectez-vous pour acceder a vos Darets</p>

      <form @submit.prevent="handleSubmit" class="space-y-5">
        <BaseInput
          id="email"
          :label="'Email'"
          type="email"
          v-model="email"
          :error="errors.email"
          required
          autocomplete="email"
        />

        <BaseInput
          id="password"
          :label="'Mot de passe'"
          type="password"
          v-model="password"
          :error="errors.password"
          required
          autocomplete="current-password"
        />

        <BaseButton type="submit" variant="primary" block :disabled="submitting">
          <span v-if="submitting" class="flex items-center gap-2">
            <svg class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" class="opacity-25" />
              <path fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" class="opacity-75" />
            </svg>
            Connexion...
          </span>
          <span v-else>{{ t('auth.login') }}</span>
        </BaseButton>
      </form>

      <p class="mt-6 text-center text-sm text-white/60">
        Pas encore de compte ?
        <RouterLink to="/inscription" class="font-medium text-primary hover:text-primaryHover">
          Creer un compte
        </RouterLink>
      </p>
    </div>
  </div>
</template>
