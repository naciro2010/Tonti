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

const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  phone: '',
})
const errors = ref<Record<string, string>>({})
const submitting = ref(false)

function validate() {
  errors.value = {}
  if (!form.value.firstName.trim()) errors.value.firstName = 'Le prenom est requis'
  if (!form.value.lastName.trim()) errors.value.lastName = 'Le nom est requis'
  if (!form.value.email.trim()) errors.value.email = 'L\'email est requis'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) errors.value.email = 'Email invalide'
  if (!form.value.password) errors.value.password = 'Le mot de passe est requis'
  else if (form.value.password.length < 8) errors.value.password = 'Minimum 8 caracteres'
  if (form.value.password !== form.value.confirmPassword) errors.value.confirmPassword = 'Les mots de passe ne correspondent pas'
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  try {
    const response = await auth.register({
      firstName: form.value.firstName.trim(),
      lastName: form.value.lastName.trim(),
      email: form.value.email.trim(),
      password: form.value.password,
      phone: form.value.phone.trim() || undefined,
    })
    if (response.success) {
      toast.success('Compte cree avec succes !')
      router.push('/mes-darets')
    } else {
      toast.error(response.message || 'Erreur lors de l\'inscription')
    }
  } catch (e: any) {
    toast.error(e.message || 'Erreur lors de l\'inscription')
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
            <path stroke-linecap="round" stroke-linejoin="round" d="M18 7.5v3m0 0v3m0-3h3m-3 0h-3m-2.25-4.125a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zM3 19.235v-.11a6.375 6.375 0 0112.75 0v.109A12.318 12.318 0 019.374 21c-2.331 0-4.512-.645-6.374-1.766z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold">Creer un compte</h1>
        <p class="mt-2 text-sm text-white/60">Inscrivez-vous pour creer ou rejoindre un Daret</p>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <BaseInput
            id="firstName"
            label="Prenom"
            v-model="form.firstName"
            :error="errors.firstName"
            required
            autocomplete="given-name"
          />
          <BaseInput
            id="lastName"
            label="Nom"
            v-model="form.lastName"
            :error="errors.lastName"
            required
            autocomplete="family-name"
          />
        </div>

        <BaseInput
          id="email"
          label="Email"
          type="email"
          v-model="form.email"
          :error="errors.email"
          required
          autocomplete="email"
        />

        <BaseInput
          id="phone"
          label="Telephone (optionnel)"
          type="tel"
          v-model="form.phone"
          hint="+212 6XX XXX XXX"
          autocomplete="tel"
        />

        <BaseInput
          id="password"
          label="Mot de passe"
          type="password"
          v-model="form.password"
          :error="errors.password"
          hint="Minimum 8 caracteres"
          required
          autocomplete="new-password"
        />

        <BaseInput
          id="confirmPassword"
          label="Confirmer le mot de passe"
          type="password"
          v-model="form.confirmPassword"
          :error="errors.confirmPassword"
          required
          autocomplete="new-password"
        />

        <BaseButton type="submit" variant="primary" block :loading="submitting">
          {{ submitting ? 'Inscription...' : 'Creer mon compte' }}
        </BaseButton>
      </form>

      <p class="mt-6 text-center text-sm text-white/60">
        Deja un compte ?
        <RouterLink to="/login" class="font-semibold text-primary no-underline transition-colors hover:text-primaryHover">
          Se connecter
        </RouterLink>
      </p>
    </div>
  </div>
</template>
