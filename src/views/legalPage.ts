import type { LocaleMessages } from "@lib/i18n";

type Key = "legal" | "privacy" | "terms";

export function renderLegalPage(messages: LocaleMessages, key: Key): HTMLElement {
  const section = document.createElement("section");
  section.className = "page simple";

  const data = messages[key];
  const title = document.createElement("h1");
  title.textContent = data.title;
  section.appendChild(title);

  const paragraph = document.createElement("p");
  paragraph.textContent = data.description;
  section.appendChild(paragraph);

  return section;
}
