export type CategoryId = "aid" | "mariage" | "naissance" | "solidarite" | "anniversaire" | "autre";

export type Category = {
  id: CategoryId;
  label: { fr: string; ar: string };
  icon: string;
};

export const categories: Category[] = [
  { id: "aid", label: { fr: "Aïd", ar: "عيد" }, icon: "🌙" },
  { id: "mariage", label: { fr: "Mariage", ar: "زفاف" }, icon: "💍" },
  { id: "naissance", label: { fr: "Naissance", ar: "مولود" }, icon: "👶" },
  { id: "solidarite", label: { fr: "Solidarité", ar: "تضامن" }, icon: "🤝" },
  { id: "anniversaire", label: { fr: "Anniversaire", ar: "عيد ميلاد" }, icon: "🎂" },
  { id: "autre", label: { fr: "Autre", ar: "أخرى" }, icon: "✨" }
];
