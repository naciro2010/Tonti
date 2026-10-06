<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import LegalPage from '@/components/LegalPage.vue';
import { LEGAL } from '@/config/legal';

const { t } = useI18n();
</script>

<template>
  <LegalPage :title="t('legal.privacy')">
    <p>
      La présente politique décrit comment {{ LEGAL.companyName }} ({{ LEGAL.address }}), responsable de
      traitement, collecte et utilise vos données personnelles lorsque vous utilisez l’application et le site
      Tonti, conformément au Règlement général sur la protection des données (RGPD) et à la loi Informatique
      et Libertés.
    </p>

    <h2>1. Données collectées</h2>
    <ul>
      <li>
        <strong>Compte</strong> : prénom, nom, adresse e-mail, mot de passe (stocké uniquement sous forme
        chiffrée), numéro de téléphone (facultatif).
      </li>
      <li>
        <strong>Darets</strong> : Darets créés ou rejoints, rôle, ordre de passage, historique des
        cotisations.
      </li>
      <li>
        <strong>Paiements</strong> : montant, devise, date, statut et référence de transaction. Les données de
        carte bancaire sont saisies directement chez notre prestataire de paiement Stripe et ne transitent
        jamais par nos serveurs.
      </li>
      <li>
        <strong>Données techniques</strong> : journaux de sécurité (adresse IP, date de connexion) conservés
        pour prévenir la fraude.
      </li>
    </ul>
    <p>Tonti n’utilise ni publicité, ni traceur publicitaire, ni suivi entre applications ou sites tiers.</p>

    <h2>2. Finalités et bases légales</h2>
    <ul>
      <li>Fournir le service (gestion du compte, des Darets et des cotisations) : exécution du contrat.</li>
      <li>Traiter les paiements et prévenir la fraude : exécution du contrat et intérêt légitime.</li>
      <li>
        Conserver les justificatifs de paiement : obligation légale (comptabilité, lutte contre le
        blanchiment).
      </li>
      <li>Vous envoyer des notifications liées à vos Darets : exécution du contrat.</li>
    </ul>

    <h2>3. Destinataires</h2>
    <ul>
      <li>
        Les autres membres de vos Darets (prénom, nom, statut de cotisation) ; votre e-mail et votre téléphone
        ne leur sont pas communiqués.
      </li>
      <li>{{ LEGAL.paymentProvider }}, prestataire de services de paiement agréé.</li>
      <li>Notre hébergeur : {{ LEGAL.host }}.</li>
    </ul>
    <p>
      Certains prestataires peuvent traiter des données hors de l’Union européenne ; ces transferts sont
      encadrés par des clauses contractuelles types de la Commission européenne ou le Data Privacy Framework.
    </p>

    <h2>4. Durées de conservation</h2>
    <ul>
      <li>Compte : jusqu’à sa suppression.</li>
      <li>
        Justificatifs de paiement : 10 ans (article L123-22 du Code de commerce), sous forme anonymisée après
        suppression du compte.
      </li>
      <li>Journaux de sécurité : 12 mois.</li>
    </ul>

    <h2>5. Suppression du compte</h2>
    <p>
      Vous pouvez supprimer votre compte à tout moment depuis l’application (Compte → Supprimer mon compte).
      Vos données d’identification sont alors effacées et les données de paiement à conserver sont
      anonymisées. La suppression n’est pas possible pendant un Daret en cours, afin de préserver les droits
      des autres membres.
    </p>

    <h2>6. Vos droits</h2>
    <p>
      Vous disposez des droits d’accès, de rectification, d’effacement, de limitation, d’opposition et de
      portabilité. Pour les exercer : <a :href="`mailto:${LEGAL.privacyEmail}`">{{ LEGAL.privacyEmail }}</a
      >. Vous pouvez également introduire une réclamation auprès de la CNIL (www.cnil.fr).
    </p>

    <h2>7. Sécurité</h2>
    <p>
      Les échanges sont chiffrés (HTTPS), les mots de passe sont hachés, les jetons de session sont stockés
      dans le trousseau sécurisé de votre appareil et les paiements sont authentifiés (3-D Secure) par Stripe.
    </p>
  </LegalPage>
</template>
