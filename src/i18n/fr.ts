export default {
  navigation: {
    home: 'Accueil',
    howItWorks: 'Comment ça marche',
    faq: 'FAQ',
    help: 'Aide',
    legal: 'CGU',
    privacy: 'Confidentialité',
    create: 'Créer une cagnotte',
    login: 'Connexion',
  },
  hero: {
    title: 'La cagnotte digitale des Marocains',
    subtitle:
      'Créez, partagez et pilotez vos collectes solidaires en toute conformité avec la loi 15-18.',
    createCta: 'Je lance ma cagnotte',
    exploreCta: 'Explorer les cagnottes',
  },
  home: {
    categoriesTitle: 'Catégories populaires',
    featuredTitle: 'Top cagnottes',
    complianceBadge: 'Conforme à la loi 15-18',
    timeline: {
      create: 'Créer',
      share: 'Partager',
      contribute: 'Contribuer',
      follow: 'Suivre',
    },
  },
  footer: {
    copyright: '© {year} Tonti. Tous droits réservés.',
    description:
      'Tonti est une plateforme marocaine de financement participatif à vocation solidaire. Service 100 % statique et sécurisé.',
    legalReminder: 'Campagnes validées selon la loi 15-18 du 22 février 2021.',
  },
  login: {
    title: 'Connexion à Tonti',
    subtitle: 'Accédez à votre tableau de bord pour gérer vos cagnottes.',
    emailLabel: 'Adresse email',
    emailPlaceholder: 'vous@exemple.ma',
    passwordLabel: 'Mot de passe',
    passwordPlaceholder: 'Votre mot de passe',
    remember: 'Se souvenir de moi',
    submit: 'Se connecter',
    socialComingSoon: 'Connexion sociale bientôt disponible',
    noAccount: 'Pas encore de compte ?',
    createOne: 'Créez-en un en quelques secondes.',
  },
  create: {
    heading: 'Créer une cagnotte',
    intro:
      'Complétez les informations suivantes pour préparer votre cagnotte et la partager à votre communauté.',
    step: {
      description: 'Description',
      illustration: 'Illustration',
      visibility: 'Visibilité',
      amounts: 'Montants',
      invitations: 'Invitations',
    },
    description: {
      titleLabel: 'Titre de la cagnotte',
      titlePlaceholder: 'Ex: Solidarité pour une association locale',
      descriptionLabel: 'Description détaillée',
      descriptionPlaceholder: 'Présentez le contexte, l’objectif et l’utilisation des fonds…',
      categoryLabel: 'Catégorie',
    },
    illustration: {
      uploadLabel: 'Télécharger une image (16:9)',
      helper: 'Formats acceptés : JPG, PNG. Recadrage automatique au format 16:9.',
      changeButton: 'Changer d’image',
    },
    visibility: {
      public: 'Publique',
      private: 'Privée',
      unlisted: 'Non listée',
      helper: 'Contrôlez qui peut trouver votre cagnotte sur Tonti.',
    },
    amounts: {
      goalLabel: 'Objectif (MAD)',
      minContributionLabel: 'Contribution minimale (MAD)',
      quickOptionsLabel: 'Options rapides',
      addOption: 'Ajouter un montant',
    },
    invitations: {
      shareTitle: 'Partagez votre cagnotte',
      shareDescription:
        'Diffusez votre cagnotte avec un lien unique, un QR code ou vos réseaux préférés.',
      copyLink: 'Copier le lien',
      copied: 'Lien copié',
      whatsapp: 'Partager sur WhatsApp',
      email: 'Partager par email',
      qrLabel: 'QR Code',
    },
    actions: {
      previous: 'Étape précédente',
      next: 'Étape suivante',
      submit: 'Publier la cagnotte',
      saving: 'Sauvegarde…',
      saved: 'Brouillon sauvegardé',
    },
  },
  cagnotte: {
    contributeCta: 'Je participe',
    contributeTitle: 'Participer à la cagnotte',
    amountLabel: 'Montant',
    anonymous: 'Contribution anonyme',
    payButton: 'Payer',
    latestContributors: 'Contributeurs récents',
    summary: 'Résumé',
    goalReached: 'objectif atteint à',
    closed: 'Cagnotte terminée',
  },
  how: {
    title: 'Comment fonctionne Tonti ?',
    steps: [
      {
        title: '1. Créez',
        description: 'Définissez votre cagnotte en 5 étapes avec validation en temps réel.',
      },
      {
        title: '2. Partagez',
        description: 'Diffusez votre lien sécurisé ou votre QR code en un clic.',
      },
      {
        title: '3. Collectez',
        description: 'Recevez les contributions en temps réel via notre partenaire de paiement.',
      },
      {
        title: '4. Suivez',
        description: 'Analysez les contributions et clôturez en toute transparence.',
      },
    ],
  },
  faq: {
    title: 'Questions fréquentes',
    items: [
      {
        question: 'Tonti est-elle conforme à la loi 15-18 ?',
        answer:
          'Oui, toutes nos cagnottes sont revues pour respecter la loi sur le financement collaboratif et les exigences de Bank Al-Maghrib.',
      },
      {
        question: 'Comment sont sécurisées les contributions ?',
        answer:
          'Les paiements sont simulés ici mais connectables à une API certifiée, avec chiffrement des données et audits réguliers.',
      },
      {
        question: 'Puis-je rendre ma cagnotte privée ?',
        answer:
          'Absolument. Vous choisissez entre publique, privée ou non listée pour maîtriser votre diffusion.',
      },
    ],
  },
  help: {
    title: 'Centre d’aide',
    intro:
      'Besoin d’assistance ? Notre équipe répond en moins de 24h à toutes vos questions.',
    contactEmail: 'support@tonti.app',
    contactPhone: '+212 5 22 00 00 00',
  },
  terms: {
    title: 'Conditions générales d’utilisation',
    intro:
      'Ce document décrit les conditions d’accès et d’utilisation de la plateforme Tonti.',
  },
  privacy: {
    title: 'Politique de confidentialité',
    intro: 'Découvrez comment nous protégeons vos données personnelles.',
  },
};
