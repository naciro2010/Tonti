export type Locale = 'fr' | 'ar';

export type TranslationKey = keyof typeof translations;

export const translations = {
  'app.name': {
    fr: 'Tonti',
    ar: 'طنطي',
  },
  'app.tagline': {
    fr: 'La cagnotte digitale conforme à la loi 15-18',
    ar: 'المنصة الرقمية للتمويل التضامني المطابقة للقانون 15-18',
  },
  'app.description': {
    fr: 'Créez, partagez et suivez vos cagnottes solidaires avec transparence et sécurité.',
    ar: 'أنشئ وشارك وتابع حملاتك التضامنية بكل شفافية وأمان.',
  },
  'cta.create': {
    fr: 'Créer une cagnotte',
    ar: 'إنشاء حملة',
  },
  'cta.login': {
    fr: 'Se connecter',
    ar: 'تسجيل الدخول',
  },
  'nav.how': {
    fr: 'Comment ça marche',
    ar: 'كيف تعمل',
  },
  'nav.faq': {
    fr: 'FAQ',
    ar: 'الأسئلة الشائعة',
  },
  'nav.support': {
    fr: 'Aide',
    ar: 'مساعدة',
  },
  'nav.legal': {
    fr: 'Conditions',
    ar: 'الشروط',
  },
  'nav.privacy': {
    fr: 'Confidentialité',
    ar: 'الخصوصية',
  },
  'home.categories.title': {
    fr: 'Catégories populaires',
    ar: 'الفئات الأكثر انتشارًا',
  },
  'home.hero.headline': {
    fr: 'La cagnotte digitale des Marocains, sécurisée et inclusive.',
    ar: 'منصة الادخار الرقمية للمغاربة، آمنة وشاملة.',
  },
  'home.stats.title': {
    fr: 'Statistiques en temps réel',
    ar: 'إحصائيات آنية',
  },
  'home.stats.description': {
    fr: 'Une transparence totale sur vos collectes.',
    ar: 'شفافية كاملة حول حملاتكم.',
  },
  'home.top.title': {
    fr: 'Cagnottes en lumière',
    ar: 'حملات مميزة',
  },
  'home.legal.banner': {
    fr: 'Tonti est conforme à la loi 15-18 sur le financement collaboratif au Maroc.',
    ar: 'طنطي متوافقة مع القانون 18-15 للتمويل التعاوني في المغرب.',
  },
  'form.next': {
    fr: 'Suivant',
    ar: 'التالي',
  },
  'form.prev': {
    fr: 'Précédent',
    ar: 'السابق',
  },
  'form.finish': {
    fr: 'Finaliser',
    ar: 'إنهاء',
  },
  'form.save.success': {
    fr: 'Brouillon enregistré',
    ar: 'تم حفظ المسودة',
  },
  'form.validation.required': {
    fr: 'Complétez les champs avant de continuer.',
    ar: 'يرجى استكمال الخانات قبل المتابعة.',
  },
  'form.validation.error': {
    fr: 'Veuillez vérifier les champs requis.',
    ar: 'تحقق من الخانات المطلوبة.',
  },
  'create.step1.title': {
    fr: 'Description',
    ar: 'الوصف',
  },
  'create.step2.title': {
    fr: 'Illustration',
    ar: 'الصورة',
  },
  'create.step3.title': {
    fr: 'Visibilité',
    ar: 'الظهور',
  },
  'create.step4.title': {
    fr: 'Montants',
    ar: 'المبالغ',
  },
  'create.step5.title': {
    fr: 'Invitations',
    ar: 'الدعوات',
  },
  'create.autosave': {
    fr: 'Votre progression est enregistrée automatiquement sur cet appareil.',
    ar: 'يتم حفظ تقدمك تلقائيًا على هذا الجهاز.',
  },
  'create.title.label': {
    fr: 'Titre',
    ar: 'العنوان',
  },
  'create.description.label': {
    fr: 'Description',
    ar: 'الوصف',
  },
  'create.description.helper': {
    fr: "Ajoutez le contexte, le besoin et l'impact.",
    ar: 'أضف السياق والحاجة والأثر.',
  },
  'create.category.label': {
    fr: 'Catégorie',
    ar: 'الفئة',
  },
  'create.visibility.legend': {
    fr: 'Choisissez la visibilité',
    ar: 'اختر مستوى الظهور',
  },
  'create.visibility.public': {
    fr: 'Publique',
    ar: 'عمومية',
  },
  'create.visibility.public.helper': {
    fr: 'Référencée et visible par tous.',
    ar: 'مرئية للجميع ومفهرسة.',
  },
  'create.visibility.private': {
    fr: 'Privée',
    ar: 'خاصة',
  },
  'create.visibility.private.helper': {
    fr: 'Accessible uniquement aux invités.',
    ar: 'يمكن الوصول إليها للمدعوين فقط.',
  },
  'create.visibility.unlisted': {
    fr: 'Non listée',
    ar: 'غير مدرجة',
  },
  'create.visibility.unlisted.helper': {
    fr: 'Accès via lien direct.',
    ar: 'الوصول عبر رابط مباشر.',
  },
  'create.goal.label': {
    fr: 'Objectif (MAD)',
    ar: 'الهدف (درهم)',
  },
  'create.minContribution.label': {
    fr: 'Contribution minimum (MAD)',
    ar: 'أدنى مساهمة (درهم)',
  },
  'create.quickOptions.label': {
    fr: 'Options rapides',
    ar: 'مبالغ سريعة',
  },
  'create.invitation.label': {
    fr: 'Message d\'invitation',
    ar: 'رسالة الدعوة',
  },
  'create.share.helper': {
    fr: 'Partagez ce lien unique avec vos proches.',
    ar: 'شارك هذا الرابط الفريد مع مجتمعك.',
  },
  'create.share.qr': {
    fr: 'QR Code à télécharger et partager.',
    ar: 'رمز QR للتحميل والمشاركة.',
  },
  'share.copy': {
    fr: 'Copier le lien',
    ar: 'نسخ الرابط',
  },
  'share.whatsapp': {
    fr: 'Partager sur WhatsApp',
    ar: 'مشاركة عبر واتساب',
  },
  'share.email': {
    fr: 'Partager par email',
    ar: 'مشاركة عبر البريد',
  },
  'contribute.cta': {
    fr: 'Je participe',
    ar: 'أشارك',
  },
  'contribute.modal.title': {
    fr: 'Je participe',
    ar: 'أشارك',
  },
  'contribute.modal.description': {
    fr: 'Choisissez un montant sécurisé et validez la simulation de paiement.',
    ar: 'اختر مبلغاً آمناً وأكد محاكاة الدفع.',
  },
  'contribute.amount.placeholder': {
    fr: 'Montant personnalisé',
    ar: 'مبلغ مخصص',
  },
  'contribute.pay': {
    fr: 'Payer',
    ar: 'الدفع',
  },
  'contribute.anonymous': {
    fr: 'Participer en anonyme',
    ar: 'المشاركة بشكل مجهول',
  },
  'contribute.collected': {
    fr: 'Montant collecté',
    ar: 'المبلغ المجموع',
  },
  'contribute.goal': {
    fr: 'Objectif',
    ar: 'الهدف',
  },
  'contribute.simulated.success': {
    fr: 'Contribution simulée',
    ar: 'تمت محاكاة المساهمة',
  },
  'contribute.simulated.error': {
    fr: 'Simulation indisponible',
    ar: 'المحاكاة غير متاحة',
  },
  'status.closed': {
    fr: 'Cagnotte terminée',
    ar: 'تم إغلاق الحملة',
  },
  'footer.rights': {
    fr: '© 2024 Tonti. Tous droits réservés.',
    ar: '© 2024 طنطي. جميع الحقوق محفوظة.',
  },
  'toggle.language': {
    fr: 'العربية',
    ar: 'Français',
  },
  'auth.login.title': {
    fr: 'Connexion sécurisée',
    ar: 'تسجيل دخول آمن',
  },
  'auth.login.description': {
    fr: 'Accédez à votre espace organisateur. Les options sociales seront activées après homologation.',
    ar: 'ادخل إلى مساحة المنظم. سيتم تفعيل الدخول الاجتماعي بعد الاعتماد.',
  },
  'auth.login.email': {
    fr: 'Email professionnel',
    ar: 'البريد المهني',
  },
  'auth.login.magic': {
    fr: 'Recevoir un lien magique',
    ar: 'استلام رابط آمن',
  },
  'auth.login.socialSoon': {
    fr: 'Connexion sociale (bientôt) :',
    ar: 'تسجيل عبر الشبكات (قريباً):',
  },
  'auth.login.google': {
    fr: 'Google (bientôt)',
    ar: 'جوجل (قريباً)',
  },
  'auth.login.apple': {
    fr: 'Apple (bientôt)',
    ar: 'آبل (قريباً)',
  },
  'auth.login.open': {
    fr: 'Ouvrir la modale',
    ar: 'فتح النافذة',
  },
};

export const defaultLocale: Locale = 'fr';

export function translate(key: TranslationKey, locale: Locale): string {
  const entry = translations[key];
  return entry?.[locale] ?? entry?.[defaultLocale] ?? key;
}
