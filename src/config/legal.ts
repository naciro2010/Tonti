/**
 * Informations légales affichées dans l'application (mentions légales, CGU, confidentialité).
 * ⚠️ À compléter avec les informations réelles de la société avant toute mise en production
 * et avant la soumission App Store (Apple vérifie l'URL de la politique de confidentialité).
 */
export const LEGAL = {
  companyName: 'Tonti SAS',
  legalForm: 'SAS au capital de [montant] €',
  registration: 'RCS [ville] [numéro SIREN]',
  address: '[adresse du siège social], France',
  publicationDirector: '[nom du directeur de la publication]',
  supportEmail: 'support@tonti.app',
  privacyEmail: 'privacy@tonti.app',
  host: '[hébergeur de l’API et de la base de données, adresse]',
  paymentProvider: 'Stripe Payments Europe, Ltd. (1 Grand Canal Street Lower, Dublin 2, Irlande)',
  lastUpdated: '2026-10-06',
} as const;
