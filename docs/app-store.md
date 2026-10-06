# Publication sur l'App Store

Ce guide part du dépôt tel quel et va jusqu'à la soumission à la revue Apple. Le build, la signature
et l'envoi sont automatisés par **fastlane** (`ios/fastlane`) et le workflow GitHub Actions
**iOS release** (`.github/workflows/ios-release.yml`, runner macOS). Aucun Mac n'est nécessaire
au quotidien ; un Mac reste utile pour tester sur simulateur et créer le certificat la première fois.

## Architecture de l'application iOS

| Élément | Choix |
| --- | --- |
| Conteneur natif | Capacitor 8 (WKWebView), projet Xcode dans `ios/App`, dépendances via Swift Package Manager |
| Identifiant | `app.tonti.ios` (modifiable : `capacitor.config.ts`, `APP_IDENTIFIER`) |
| Cible minimale | iOS 15, iPhone, portrait |
| Code web | embarqué dans l'application (aucun code chargé à distance, guideline 2.5.2) |
| Jetons de session | Keychain (`@aparajita/capacitor-secure-storage`) |
| Paiement | page Stripe Checkout ouverte dans `SFSafariViewController` (`@capacitor/browser`), retour par deep link `tonti://paiement/resultat` |
| Manifeste de confidentialité | `ios/App/App/PrivacyInfo.xcprivacy` |

## 1. Prérequis Apple (une seule fois)

1. **Apple Developer Program au nom de la société** (99 €/an, numéro D-U-N-S requis).
   Une application qui fait circuler de l'argent entre utilisateurs relève des services financiers :
   Apple exige qu'elle soit publiée par une personne morale, pas par un compte individuel
   (guideline 5.1.1(ix)).
2. Dans *Certificates, Identifiers & Profiles* → **Identifiers** : créer l'App ID `app.tonti.ios`
   (aucune capacité particulière n'est requise).
3. Dans **App Store Connect** → *Apps* → **+** : créer l'application (nom « Tonti – Daret entre proches »,
   langue principale Français, bundle ID `app.tonti.ios`, SKU libre).
4. **Clé API App Store Connect** : *Users and Access* → *Integrations* → *App Store Connect API* →
   générer une clé avec le rôle **App Manager**. Noter le *Key ID* et l'*Issuer ID*, télécharger le `.p8`.
5. **Certificat de distribution** : *Certificates* → **+** → *Apple Distribution*. Sur un Mac, l'importer
   dans le Trousseau puis l'exporter en `.p12` protégé par un mot de passe.

## 2. Secrets et variables GitHub

Créer un environnement GitHub **`app-store`** (*Settings → Environments*), avec éventuellement une
validation manuelle, puis y ajouter :

| Secret | Valeur |
| --- | --- |
| `APPLE_TEAM_ID` | Team ID (10 caractères, *Membership details*) |
| `ASC_KEY_ID` | Key ID de la clé API |
| `ASC_ISSUER_ID` | Issuer ID |
| `ASC_KEY_P8_BASE64` | `base64 -i AuthKey_XXXX.p8` |
| `IOS_DIST_CERT_P12_BASE64` | `base64 -i distribution.p12` |
| `IOS_DIST_CERT_PASSWORD` | mot de passe du `.p12` |
| `REVIEW_FIRST_NAME`, `REVIEW_LAST_NAME`, `REVIEW_PHONE`, `REVIEW_EMAIL` | contact pour l'équipe de revue |
| `REVIEW_DEMO_USER`, `REVIEW_DEMO_PASSWORD` | compte de démonstration (voir §5) |

| Variable (*Variables*) | Valeur |
| --- | --- |
| `VITE_API_URL` | `https://api.tonti.app/api/v1` |
| `VITE_PUBLIC_WEB_URL` | `https://tonti.app` (liens d'invitation) |
| `APP_IDENTIFIER` | `app.tonti.ios` (facultatif) |

Le build mobile échoue volontairement si `VITE_API_URL` ou `VITE_PUBLIC_WEB_URL` ne sont pas en `https://`.

## 3. Envoyer un build sur TestFlight

- **Actions → iOS release → Run workflow → lane `beta`**, ou pousser un tag : `git tag v1.1.0 && git push origin v1.1.0`.
- La version affichée vient de `package.json` (`version`) ; le numéro de build est incrémenté
  automatiquement à partir du dernier build TestFlight.
- L'IPA et les dSYM sont aussi disponibles en artefact du workflow.

Le build apparaît dans TestFlight après le traitement par Apple (10 à 30 min). Ajouter des testeurs
internes et valider le parcours complet : inscription, création, invitation, démarrage, paiement
(Stripe en mode test : carte `4242 4242 4242 4242`), retour dans l'application, suppression du compte.

## 4. Fiche App Store

Textes FR et AR versionnés dans `ios/fastlane/metadata` (nom, sous-titre, description, mots-clés,
URLs de support et de confidentialité, notes de version). Après modification :
**Run workflow → lane `metadata`**.

À compléter dans App Store Connect (non automatisé) :

- **Captures d'écran** iPhone 6,9" (1320 × 2868 px) obligatoires ; les déposer dans
  `ios/fastlane/screenshots/fr-FR` et passer `skip_screenshots(false)` dans le `Deliverfile`, ou les
  téléverser à la main.
- **Classification par âge** : répondre « Non » à toutes les rubriques de contenu ; les CGU réservant
  le service aux majeurs, sélectionner une classification 18+.
- **Confidentialité de l'app** (*App Privacy*) — à déclarer exactement ainsi, en cohérence avec
  `PrivacyInfo.xcprivacy` :

  | Donnée | Collectée | Liée à l'identité | Suivi (tracking) | Finalité |
  | --- | --- | --- | --- | --- |
  | Nom | Oui | Oui | Non | Fonctionnalités de l'app |
  | Adresse e-mail | Oui | Oui | Non | Fonctionnalités de l'app |
  | Numéro de téléphone (facultatif) | Oui | Oui | Non | Fonctionnalités de l'app |
  | Identifiant utilisateur | Oui | Oui | Non | Fonctionnalités de l'app |
  | Historique d'achats (cotisations) | Oui | Oui | Non | Fonctionnalités de l'app |

  Les données de carte bancaire sont collectées par Stripe sur sa propre page, pas par l'application.
  Pas de publicité, pas d'analytics dans l'app native, pas d'App Tracking Transparency nécessaire.

- **Prix** : gratuit.
- **Conformité export** : déjà déclarée dans `Info.plist` (`ITSAppUsesNonExemptEncryption = false`).

## 5. Préparer la revue Apple

1. **Compte de démonstration** sur l'API de production, avec une Daret « Daret démo » déjà démarrée
   dont le compte démo n'est pas le bénéficiaire du tour en cours (pour que le bouton « Payer » soit visible).
   Si la production est en mode Stripe *live*, prévoir un remboursement ou utiliser un environnement
   de recette en mode test et l'indiquer dans les notes.
2. Vérifier `ios/fastlane/review_notes.txt` (parcours de test et justification du paiement hors IAP).
3. **Run workflow → lane `release`** avec « Soumettre à la revue » coché (publication manuelle et
   déploiement progressif sur 7 jours une fois l'app acceptée).

### Points de conformité déjà traités

| Guideline | Traitement |
| --- | --- |
| 3.1.1 / 3.1.3(e) / 3.1.5 — achats intégrés | Les cotisations sont des transferts d'argent réel entre membres pour un arrangement financier hors de l'app : paiement via Stripe, pas d'IAP. Expliqué dans les notes de revue. |
| 5.1.1(v) — suppression de compte | Compte → Supprimer mon compte (données anonymisées côté serveur). |
| 5.1.1(i) — politique de confidentialité | Page `/confidentialite` dans l'app + URL dans la fiche. |
| 5.1.2 — suivi | Aucun traceur ; `NSPrivacyTracking = false`. |
| 4.8 — Sign in with Apple | Non requis : pas de connexion via un service tiers (e-mail + mot de passe uniquement). |
| 4.2 — fonctionnalités minimales | Navigation native par onglets, deep links, Keychain, splash, safe areas, expérience complète hors site web. |
| 2.5.2 — code téléchargé | Bundle web embarqué, aucune mise à jour à chaud. |
| Manifeste de confidentialité | `PrivacyInfo.xcprivacy` (raison `CA92.1` pour UserDefaults). |

## 6. Développement local (Mac)

```bash
cp .env.example .env.mobile.local   # renseigner des URLs https (ex : tunnel vers l'API locale)
npm ci
npm run cap:ios                     # build mobile + cap sync + ouverture dans Xcode
```

Régénérer icône et splash après modification de `assets/icon-only.png` / `assets/splash.png` :
`npm run assets:generate`. L'icône actuelle est provisoire et doit être remplacée par la version
définitive du designer (1024 × 1024, sans transparence).
