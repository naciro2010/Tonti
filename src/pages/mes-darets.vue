<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/composables/useAuthStore'
import { useToast } from '@/composables/useToast'
import { daretApi, type DaretResponse } from '@/services/api'
import BaseButton from '@/components/BaseButton.vue'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

const darets = ref<DaretResponse[]>([])
const loading = ref(true)

onMounted(async () => {
  if (!auth.isAuthenticated.value) {
    router.replace('/login')
    return
  }
  try {
    const response = await daretApi.list()
    if (response.success && response.data) {
      darets.value = response.data
    }
  } catch (e: any) {
    toast.error('Impossible de charger vos Darets')
  } finally {
    loading.value = false
  }
})

const activeDarets = computed(() => darets.value.filter(d => d.etat === 'ACTIVE' || d.etat === 'VERROUILLEE'))
const recruitingDarets = computed(() => darets.value.filter(d => d.etat === 'RECRUTEMENT'))
const completedDarets = computed(() => darets.value.filter(d => d.etat === 'TERMINEE'))

function statusLabel(etat: string) {
  const map: Record<string, string> = {
    RECRUTEMENT: 'Recrutement',
    VERROUILLEE: 'Verrouillee',
    ACTIVE: 'Active',
    TERMINEE: 'Terminee',
    ANNULEE: 'Annulee',
  }
  return map[etat] || etat
}

function statusColor(etat: string) {
  const map: Record<string, string> = {
    RECRUTEMENT: 'bg-blue-500/20 text-blue-400',
    VERROUILLEE: 'bg-yellow-500/20 text-yellow-400',
    ACTIVE: 'bg-green-500/20 text-green-400',
    TERMINEE: 'bg-white/10 text-white/50',
    ANNULEE: 'bg-red-500/20 text-red-400',
  }
  return map[etat] || 'bg-white/10 text-white/50'
}

function formatCurrency(amount: number, devise: string) {
  const symbols: Record<string, string> = { MAD: 'DH', EUR: '\u20AC', USD: '$' }
  return `${amount} ${symbols[devise] || devise}`
}
</script>

<template>
  <div class="py-6">
    <!-- Header -->
    <div class="mb-8 flex flex-wrap items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">Mes Darets</h1>
        <p class="mt-1 text-sm text-white/60">Gerez vos cagnottes rotatives</p>
      </div>
      <div class="flex gap-3">
        <BaseButton variant="secondary" @click="router.push('/daret/rejoindre')">
          {{ t('nav.join') }}
        </BaseButton>
        <BaseButton variant="primary" @click="router.push('/daret/creer')">
          {{ t('actions.create') }}
        </BaseButton>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <div v-for="i in 3" :key="i" class="card animate-pulse p-6">
        <div class="h-5 w-48 rounded bg-white/10" />
        <div class="mt-3 h-4 w-32 rounded bg-white/5" />
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="darets.length === 0" class="card flex flex-col items-center py-16 text-center">
      <div class="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-primary/10">
        <svg class="h-8 w-8 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
        </svg>
      </div>
      <h2 class="mb-2 text-lg font-semibold">Aucun Daret</h2>
      <p class="mb-6 max-w-sm text-sm text-white/60">
        Vous n'avez pas encore de Daret. Creez-en un ou rejoignez un Daret existant avec un code d'invitation.
      </p>
      <div class="flex gap-3">
        <BaseButton variant="secondary" @click="router.push('/daret/rejoindre')">Rejoindre un Daret</BaseButton>
        <BaseButton variant="primary" @click="router.push('/daret/creer')">Creer un Daret</BaseButton>
      </div>
    </div>

    <!-- Daret List -->
    <template v-else>
      <!-- Active Darets -->
      <section v-if="activeDarets.length > 0" class="mb-8">
        <h2 class="mb-4 text-lg font-semibold text-white/80">En cours</h2>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <RouterLink
            v-for="daret in activeDarets"
            :key="daret.id"
            :to="`/daret/${daret.id}`"
            class="card group p-5 transition hover:border-primary/30"
          >
            <div class="mb-3 flex items-center justify-between">
              <h3 class="font-semibold group-hover:text-primary">{{ daret.nom }}</h3>
              <span class="rounded-full px-2.5 py-0.5 text-xs font-medium" :class="statusColor(daret.etat)">
                {{ statusLabel(daret.etat) }}
              </span>
            </div>
            <div class="space-y-1.5 text-sm text-white/60">
              <div class="flex justify-between">
                <span>Montant mensuel</span>
                <span class="font-medium text-white">{{ formatCurrency(daret.montantMensuel, daret.devise) }}</span>
              </div>
              <div class="flex justify-between">
                <span>Membres</span>
                <span>{{ daret.membresCount }}/{{ daret.taille }}</span>
              </div>
              <div v-if="daret.currentRound" class="flex justify-between">
                <span>Round</span>
                <span>{{ daret.currentRound }}/{{ daret.taille }}</span>
              </div>
            </div>
          </RouterLink>
        </div>
      </section>

      <!-- Recruiting -->
      <section v-if="recruitingDarets.length > 0" class="mb-8">
        <h2 class="mb-4 text-lg font-semibold text-white/80">En recrutement</h2>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <RouterLink
            v-for="daret in recruitingDarets"
            :key="daret.id"
            :to="`/daret/${daret.id}`"
            class="card group p-5 transition hover:border-primary/30"
          >
            <div class="mb-3 flex items-center justify-between">
              <h3 class="font-semibold group-hover:text-primary">{{ daret.nom }}</h3>
              <span class="rounded-full px-2.5 py-0.5 text-xs font-medium" :class="statusColor(daret.etat)">
                {{ statusLabel(daret.etat) }}
              </span>
            </div>
            <div class="space-y-1.5 text-sm text-white/60">
              <div class="flex justify-between">
                <span>Montant</span>
                <span class="font-medium text-white">{{ formatCurrency(daret.montantMensuel, daret.devise) }}</span>
              </div>
              <div class="flex justify-between">
                <span>Membres</span>
                <span>{{ daret.membresCount }}/{{ daret.taille }}</span>
              </div>
              <div>
                <div class="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-white/10">
                  <div
                    class="h-full rounded-full bg-primary transition-all"
                    :style="{ width: `${(daret.membresCount / daret.taille) * 100}%` }"
                  />
                </div>
              </div>
            </div>
          </RouterLink>
        </div>
      </section>

      <!-- Completed -->
      <section v-if="completedDarets.length > 0">
        <h2 class="mb-4 text-lg font-semibold text-white/80">Terminees</h2>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div v-for="daret in completedDarets" :key="daret.id" class="card p-5 opacity-60">
            <div class="mb-3 flex items-center justify-between">
              <h3 class="font-semibold">{{ daret.nom }}</h3>
              <span class="rounded-full px-2.5 py-0.5 text-xs font-medium" :class="statusColor(daret.etat)">
                {{ statusLabel(daret.etat) }}
              </span>
            </div>
            <div class="text-sm text-white/50">
              {{ daret.taille }} membres &middot; {{ formatCurrency(daret.montantMensuel, daret.devise) }}/mois
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>
