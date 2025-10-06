import type { LocaleMessages } from "@lib/i18n";

export function renderHow(messages: LocaleMessages): HTMLElement {
  const section = document.createElement("section");
  section.className = "page simple";

  const title = document.createElement("h1");
  title.textContent = messages.how.title;
  section.appendChild(title);

  const list = document.createElement("ol");
  list.className = "steps-list";
  messages.how.steps.forEach((step) => {
    const item = document.createElement("li");
    const strong = document.createElement("strong");
    strong.textContent = step.title;
    item.appendChild(strong);
    const description = document.createElement("p");
    description.textContent = step.description;
    item.appendChild(description);
    list.appendChild(item);
  });
  section.appendChild(list);

  return section;
}
