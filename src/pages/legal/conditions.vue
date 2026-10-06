<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import LegalPage from '@/components/LegalPage.vue';
import { LEGAL } from '@/config/legal';

const { t } = useI18n();
</script>

<template>
  <LegalPage :title="t('legal.terms')">
    <h2>1. Éditeur</h2>
    <p>
      Tonti est édité par {{ LEGAL.companyName }}, {{ LEGAL.legalForm }}, immatriculée au
      {{ LEGAL.registration }}, dont le siège social est situé {{ LEGAL.address }}. Directeur de la
      publication : {{ LEGAL.publicationDirector }}. Contact :
      <a :href="`mailto:${LEGAL.supportEmail}`">{{ LEGAL.supportEmail }}</a
      >. Hébergement : {{ LEGAL.host }}.
    </p>

    <h2>2. Objet du service</h2>
    <p>
      Tonti permet d’organiser des Darets (tontines) : un groupe de membres verse chaque mois une cotisation
      identique et, à tour de rôle, un membre bénéficie de la cagnotte du mois. Tonti fournit les outils
      d’organisation (invitations, ordre de passage, suivi et rappels) et le paiement en ligne des
      cotisations.
    </p>

    <h2>3. Compte</h2>
    <p>
      L’inscription est réservée aux personnes majeures. Vous vous engagez à fournir des informations exactes
      et à garder votre mot de passe confidentiel. Vous pouvez supprimer votre compte à tout moment, sauf
      pendant un Daret en cours.
    </p>

    <h2>4. Fonctionnement d’un Daret</h2>
    <ul>
      <li>
        L’organisateur fixe le montant mensuel, la devise et le nombre de membres ; ces éléments ne sont plus
        modifiables après le démarrage.
      </li>
      <li>
        Au démarrage, l’ordre de passage est tiré au sort de manière équitable, sauf ordre convenu entre les
        membres.
      </li>
      <li>
        Chaque membre, hors bénéficiaire du mois, s’engage à régler sa cotisation avant la fin de la période,
        dans le délai de grâce fixé par l’organisateur.
      </li>
      <li>Un round est clôturé lorsque toutes les cotisations ont été réglées.</li>
    </ul>

    <h2>5. Paiements</h2>
    <p>
      Les paiements sont traités par {{ LEGAL.paymentProvider }}. Les données de carte sont saisies sur la
      page sécurisée de Stripe et ne sont jamais conservées par Tonti. Un paiement effectué en double est
      remboursé automatiquement.
      <!-- TODO juridique : décrire ici les modalités de reversement de la cagnotte au bénéficiaire (cf. DEPLOY.md, « Reversement des fonds ») -->
      Les modalités de reversement de la cagnotte au bénéficiaire sont précisées lors du démarrage du Daret.
    </p>

    <h2>6. Responsabilité</h2>
    <p>
      Tonti met en œuvre les moyens raisonnables pour assurer la disponibilité et la sécurité du service. Les
      engagements de cotisation sont pris entre les membres d’un Daret ; Tonti n’est pas garant de leur
      exécution par un membre défaillant.
    </p>

    <h2>7. Données personnelles</h2>
    <p>
      Le traitement de vos données est décrit dans la
      <RouterLink to="/confidentialite">politique de confidentialité</RouterLink>.
    </p>

    <h2>8. Droit applicable</h2>
    <p>
      Les présentes conditions sont soumises au droit français. En cas de litige, une solution amiable sera
      recherchée avant toute action ; le consommateur peut recourir gratuitement à un médiateur de la
      consommation.
    </p>
  </LegalPage>
</template>
