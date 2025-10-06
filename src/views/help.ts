import type { LocaleMessages } from "@lib/i18n";

export function renderHelp(messages: LocaleMessages): HTMLElement {
  const section = document.createElement("section");
  section.className = "page simple";

  const title = document.createElement("h1");
  title.textContent = messages.help.title;
  section.appendChild(title);

  const paragraph = document.createElement("p");
  paragraph.textContent = messages.help.description;
  section.appendChild(paragraph);

  return section;
}
