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
  <div class="mx-auto max-w-md py-12">
    <div class="card p-8">
      <h1 class="mb-2 text-2xl font-bold">Creer un compte</h1>
      <p class="mb-8 text-sm text-white/60">Inscrivez-vous pour creer ou rejoindre un Daret</p>

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

        <BaseButton type="submit" variant="primary" block :disabled="submitting">
          <span v-if="submitting" class="flex items-center gap-2">
            <svg class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" class="opacity-25" />
              <path fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" class="opacity-75" />
            </svg>
            Inscription...
          </span>
          <span v-else>Creer mon compte</span>
        </BaseButton>
      </form>

      <p class="mt-6 text-center text-sm text-white/60">
        Deja un compte ?
        <RouterLink to="/login" class="font-medium text-primary hover:text-primaryHover">
          Se connecter
        </RouterLink>
      </p>
    </div>
  </div>
</template>
