export type Locale = "fr" | "ar";

export const LOCALES: Record<Locale, { label: string; dir: "ltr" | "rtl" }> = {
  fr: { label: "Français", dir: "ltr" },
  ar: { label: "العربية", dir: "rtl" }
};

export type StepOption = {
  id: string;
  label: string;
  description: string;
};

export type LocaleMessages = {
  nav: {
    home: string;
    create: string;
    how: string;
    faq: string;
    help: string;
    legal: string;
  };
  footer: {
    legal: string;
    privacy: string;
    terms: string;
  };
  home: {
    heroTitle: string;
    heroSubtitle: string;
    heroCta: string;
    legalHeading: string;
    legalDescription: string;
    categoriesTitle: string;
    featuredTitle: string;
    featuredCta: string;
    cardCollected: string;
    cardGoal: string;
    cardDaysLeft: string;
    cardView: string;
  };
  create: {
    title: string;
    description: string;
    steps: StepOption[];
    descriptionStep: {
      titleLabel: string;
      titlePlaceholder: string;
      descriptionLabel: string;
      descriptionPlaceholder: string;
      categoryLabel: string;
    };
    illustrationStep: {
      intro: string;
      placeholder: string;
    };
    visibilityStep: {
      intro: string;
      options: StepOption[];
    };
    amountsStep: {
      goalLabel: string;
      minLabel: string;
      feesNotice: string;
    };
    invitationsStep: {
      intro: string;
      messagePlaceholder: string;
      note: string;
    };
    actions: {
      skip: string;
      continue: string;
    };
    toasts: {
      successTitle: string;
      successDescription: string;
      errorTitle: string;
      errorDescription: string;
    };
  };
  how: {
    title: string;
    steps: { title: string; description: string }[];
  };
  faq: {
    title: string;
    items: { question: string; answer: string; defaultOpen?: boolean }[];
  };
  help: {
    title: string;
    description: string;
  };
  legal: { title: string; description: string };
  privacy: { title: string; description: string };
  terms: { title: string; description: string };
  cagnotte: {
    notFoundTitle: string;
    notFoundDescription: string;
    backHome: string;
    organizerPrefix: string;
    progressTitle: string;
    collectedLabel: string;
    goalLabel: string;
    daysLeftLabel: string;
    participateCta: string;
    recentParticipants: string;
    participantsCount: string;
    detailsTitle: string;
    beneficiaryLabel: string;
    visibilityLabel: string;
    statusLabel: string;
    paymentNote: string;
    loginTitle: string;
    loginDescription: string;
    loginCta: string;
    visibilityMap: Record<string, string>;
    statusMap: Record<string, string>;
  };
  login: {
    title: string;
    emailLabel: string;
    passwordLabel: string;
    submit: string;
    socialNote: string;
  };
  toastClose: string;
  notFound: {
    title: string;
    description: string;
    back: string;
  };
};

export const messages: Record<Locale, LocaleMessages> = {
  fr: {
    nav: {
      home: "Accueil",
      create: "Créer une cagnotte",
      how: "Comment ça marche",
      faq: "FAQ",
      help: "Aide",
      legal: "Mentions légales"
    },
    footer: {
      legal: "Mentions légales",
      privacy: "Politique de confidentialité",
      terms: "CGU"
    },
    home: {
      heroTitle: "Tonti, la cagnotte digitale des Marocains",
      heroSubtitle:
        "Créez et partagez vos cagnottes en toute transparence pour soutenir vos proches au Maroc et à l'étranger.",
      heroCta: "Lancer ma cagnotte",
      legalHeading: "Cadre légal marocain",
      legalDescription:
        "Tonti est une plateforme de financement collaboratif en cours d'homologation. Les collectes sont simulées et ne représentent pas un service de paiement effectif.",
      categoriesTitle: "Catégories populaires",
      featuredTitle: "Cagnottes mises en avant",
      featuredCta: "Créer une cagnotte",
      cardCollected: "Collecté",
      cardGoal: "Objectif",
      cardDaysLeft: "Jours restants",
      cardView: "Voir la cagnotte"
    },
    create: {
      title: "Créer une cagnotte",
      description: "Simulez la création d'une cagnotte conforme au cadre marocain. Les paiements seront activés prochainement.",
      steps: [
        { id: "description", label: "Description", description: "Titre, objectif et catégorie" },
        { id: "illustration", label: "Illustration", description: "Image de couverture" },
        { id: "visibility", label: "Visibilité", description: "Contrôlez qui peut participer" },
        { id: "amounts", label: "Montants", description: "Objectif et contributions" },
        { id: "invitations", label: "Invitations", description: "Message de partage" }
      ],
      descriptionStep: {
        titleLabel: "Titre",
        titlePlaceholder: "Mariage de Samia & Youssef",
        descriptionLabel: "Description",
        descriptionPlaceholder: "Décrivez l'histoire de votre cagnotte...",
        categoryLabel: "Catégorie"
      },
      illustrationStep: {
        intro: "Ajoutez une image 16:9 pour illustrer votre cagnotte.",
        placeholder: "Glissez-déposez votre visuel (mock)"
      },
      visibilityStep: {
        intro: "Choisissez la visibilité",
        options: [
          { id: "publique", label: "Publique", description: "Accessible à tous" },
          { id: "privee", label: "Privée", description: "Accès via un code" },
          { id: "non-listee", label: "Non listée", description: "Lien direct uniquement" }
        ]
      },
      amountsStep: {
        goalLabel: "Objectif (MAD)",
        minLabel: "Don suggéré minimum",
        feesNotice:
          "Les frais de service seront communiqués prochainement. Aucune transaction réelle n'est opérée via Tonti pour le moment."
      },
      invitationsStep: {
        intro: "Préparez votre message de partage",
        messagePlaceholder: "Rejoignez ma cagnotte sur Tonti !",
        note: "Import de contacts et partage par QR code disponibles prochainement. Copiez/collez ce message pour prévenir vos proches."
      },
      actions: {
        skip: "Ignorer",
        continue: "Sauvegarder & continuer"
      },
      toasts: {
        successTitle: "Cagnotte sauvegardée",
        successDescription: "Nous vous contacterons dès l'ouverture des paiements.",
        errorTitle: "Complétez les informations",
        errorDescription: "Vérifiez le formulaire"
      }
    },
    how: {
      title: "Comment ça marche",
      steps: [
        { title: "1. Créez votre cagnotte", description: "Définissez un titre, une description et une catégorie pour contextualiser." },
        { title: "2. Personnalisez", description: "Ajoutez une illustration et choisissez la visibilité adaptée à votre cercle." },
        { title: "3. Partagez", description: "Diffusez le lien sécurisé auprès de vos proches et suivez les contributions." }
      ]
    },
    faq: {
      title: "Questions fréquentes",
      items: [
        {
          question: "Tonti gère-t-il les paiements ?",
          answer: "Les paiements sont simulés pour le prototype. L'intégration PSP est en cours de conformité.",
          defaultOpen: true
        },
        {
          question: "La plateforme est-elle conforme au cadre marocain ?",
          answer: "Oui, le wording et les mentions légales respectent le projet de loi sur le financement collaboratif."
        }
      ]
    },
    help: {
      title: "Aide",
      description: "Besoin d'assistance ? Contactez-nous sur support@tonti.ma ou consultez la FAQ pour une réponse immédiate."
    },
    legal: {
      title: "Mentions légales",
      description: "Ce contenu est un placeholder conforme au cadre marocain du financement collaboratif."
    },
    privacy: {
      title: "Politique de confidentialité",
      description:
        "Ce contenu présente les engagements de Tonti en matière de protection des données à caractère personnel dans le cadre marocain."
    },
    terms: {
      title: "Conditions générales d'utilisation",
      description: "Cette page décrit les règles d'utilisation du service Tonti conformément au droit marocain."
    },
    cagnotte: {
      notFoundTitle: "Cagnotte introuvable",
      notFoundDescription: "La cagnotte recherchée n'existe plus ou a été archivée.",
      backHome: "Retour à l'accueil",
      organizerPrefix: "Organisé par",
      progressTitle: "Progression",
      collectedLabel: "Collecté",
      goalLabel: "Objectif",
      daysLeftLabel: "Jours restants",
      participateCta: "Je participe (mock)",
      recentParticipants: "Participants récents",
      participantsCount: "{count} participants",
      detailsTitle: "Détails",
      beneficiaryLabel: "Bénéficiaire",
      visibilityLabel: "Visibilité",
      statusLabel: "Statut",
      paymentNote: "Les paiements par carte, virement et cash seront intégrés prochainement.",
      loginTitle: "Connexion",
      loginDescription: "Connectez-vous pour suivre vos contributions.",
      loginCta: "Ouvrir la modale de connexion",
      visibilityMap: {
        publique: "Publique",
        privee: "Privée",
        "non-listee": "Non listée"
      },
      statusMap: {
        ouverte: "Ouverte",
        close: "Clôturée"
      }
    },
    login: {
      title: "Connexion",
      emailLabel: "Email",
      passwordLabel: "Mot de passe",
      submit: "Se connecter",
      socialNote: "Connexion sociale disponible prochainement."
    },
    toastClose: "Fermer",
    notFound: {
      title: "Page introuvable",
      description: "La page demandée n'existe pas ou n'est plus disponible.",
      back: "Retour à l'accueil"
    }
  },
  ar: {
    nav: {
      home: "الرئيسية",
      create: "إنشاء حملة",
      how: "كيف تعمل",
      faq: "الأسئلة الشائعة",
      help: "مساعدة",
      legal: "إشعارات قانونية"
    },
    footer: {
      legal: "إشعارات قانونية",
      privacy: "سياسة الخصوصية",
      terms: "الشروط"
    },
    home: {
      heroTitle: "تونتي، الحملة الرقمية للمغاربة",
      heroSubtitle: "أنشئ وشارك حملاتك بكل شفافية لدعم أحبائك في المغرب وخارجه.",
      heroCta: "أنشئ حملتي",
      legalHeading: "إطار قانوني مغربي",
      legalDescription: "تونتي منصة تمويل تعاوني قيد الاعتماد. المعاملات محاكاة ولا تمثل خدمة دفع فعلية.",
      categoriesTitle: "الفئات",
      featuredTitle: "حملات مميزة",
      featuredCta: "أنشئ حملة",
      cardCollected: "المبلغ المحصل",
      cardGoal: "الهدف",
      cardDaysLeft: "الأيام المتبقية",
      cardView: "عرض الحملة"
    },
    create: {
      title: "إنشاء حملة",
      description: "هذه تجربة لإنشاء حملة متوافقة مع الإطار المغربي. سيتم تفعيل المدفوعات لاحقًا.",
      steps: [
        { id: "description", label: "الوصف", description: "العنوان، الهدف والفئة" },
        { id: "illustration", label: "الصورة", description: "صورة الغلاف" },
        { id: "visibility", label: "الظهور", description: "تحكم بمن يمكنه المشاركة" },
        { id: "amounts", label: "المبالغ", description: "الهدف والمساهمات" },
        { id: "invitations", label: "الدعوات", description: "رسالة المشاركة" }
      ],
      descriptionStep: {
        titleLabel: "العنوان",
        titlePlaceholder: "حملة زفاف سامية ويوسف",
        descriptionLabel: "الوصف",
        descriptionPlaceholder: "اشرح قصة حملتك...",
        categoryLabel: "الفئة"
      },
      illustrationStep: {
        intro: "أضف صورة بنسبة 16:9 لتمثيل حملتك.",
        placeholder: "اسحب وأفلت الصورة (محاكاة)"
      },
      visibilityStep: {
        intro: "اختر مستوى الظهور",
        options: [
          { id: "publique", label: "عامة", description: "متاحة للجميع" },
          { id: "privee", label: "خاصة", description: "ولوج عبر رمز" },
          { id: "non-listee", label: "غير مدرجة", description: "رابط مباشر فقط" }
        ]
      },
      amountsStep: {
        goalLabel: "الهدف (درهم)",
        minLabel: "الحد الأدنى المقترح",
        feesNotice: "سيتم توضيح رسوم الخدمة لاحقًا. لا تتم أي معاملة حقيقية عبر تونتي حاليًا."
      },
      invitationsStep: {
        intro: "حضّر رسالة المشاركة",
        messagePlaceholder: "انضموا إلى حملتي على تونتي!",
        note: "إمكانية استيراد جهات الاتصال ومشاركة رمز QR قريبًا. انسخ الرسالة لإبلاغ المقربين."
      },
      actions: {
        skip: "تخطي",
        continue: "حفظ ومتابعة"
      },
      toasts: {
        successTitle: "تم حفظ الحملة",
        successDescription: "سنتواصل معك فور فتح المدفوعات.",
        errorTitle: "أكمل المعلومات",
        errorDescription: "تحقق من الحقول"
      }
    },
    how: {
      title: "كيف تعمل",
      steps: [
        { title: "١. أنشئ حملتك", description: "حدد عنواناً ووصفاً وفئة لتوضيح سياق الحملة." },
        { title: "٢. خصص", description: "أضف صورة واختر مستوى الظهور الملائم لدائرتك." },
        { title: "٣. شارك", description: "شارك الرابط الآمن مع أحبائك وتابع المساهمات." }
      ]
    },
    faq: {
      title: "الأسئلة الشائعة",
      items: [
        {
          question: "هل تدير تونتي المدفوعات؟",
          answer: "المدفوعات محاكاة في هذا النموذج الأولي. تكامل مزود الخدمة قيد الامتثال.",
          defaultOpen: true
        },
        {
          question: "هل المنصة متوافقة مع الإطار المغربي؟",
          answer: "نعم، تمت مواءمة المحتوى والعبارات القانونية مع مشروع قانون التمويل التعاوني."
        }
      ]
    },
    help: {
      title: "مساعدة",
      description: "هل تحتاج إلى دعم؟ راسلنا على support@tonti.ma أو تصفح قسم الأسئلة الشائعة."
    },
    legal: {
      title: "إشعارات قانونية",
      description: "هذا المحتوى تمهيدي ومتوافق مع الإطار المغربي للتمويل التعاوني."
    },
    privacy: {
      title: "سياسة الخصوصية",
      description: "يعرض هذا المحتوى التزامات تونتي بحماية المعطيات الشخصية وفق التشريع المغربي."
    },
    terms: {
      title: "الشروط العامة للاستخدام",
      description: "تصف هذه الصفحة قواعد استخدام خدمة تونتي بما يتماشى مع القانون المغربي."
    },
    cagnotte: {
      notFoundTitle: "الحملة غير متوفرة",
      notFoundDescription: "الحملة المطلوبة غير موجودة أو تم أرشفتها.",
      backHome: "العودة إلى الرئيسية",
      organizerPrefix: "بإدارة",
      progressTitle: "التقدم",
      collectedLabel: "المبلغ المحصل",
      goalLabel: "الهدف",
      daysLeftLabel: "الأيام المتبقية",
      participateCta: "أشارك (محاكاة)",
      recentParticipants: "مساهمات حديثة",
      participantsCount: "{count} مساهم",
      detailsTitle: "تفاصيل",
      beneficiaryLabel: "المستفيد",
      visibilityLabel: "الظهور",
      statusLabel: "الحالة",
      paymentNote: "سيتم دمج وسائل الدفع المغربية قريبًا.",
      loginTitle: "تسجيل الدخول",
      loginDescription: "سجل الدخول لمتابعة مساهماتك.",
      loginCta: "افتح نافذة تسجيل الدخول",
      visibilityMap: {
        publique: "عامة",
        privee: "خاصة",
        "non-listee": "غير مدرجة"
      },
      statusMap: {
        ouverte: "مفتوحة",
        close: "مغلقة"
      }
    },
    login: {
      title: "تسجيل الدخول",
      emailLabel: "البريد الإلكتروني",
      passwordLabel: "كلمة المرور",
      submit: "دخول",
      socialNote: "دعم تسجيل الدخول الاجتماعي قريبًا."
    },
    toastClose: "إغلاق",
    notFound: {
      title: "الصفحة غير موجودة",
      description: "الصفحة المطلوبة غير متوفرة أو تم إزالتها.",
      back: "العودة إلى الرئيسية"
    }
  }
};
