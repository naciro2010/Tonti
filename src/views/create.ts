import type { Locale } from "@lib/i18n";
import type { LocaleMessages } from "@lib/i18n";
import { renderCreateStepper } from "@ui/createStepper";

export function renderCreate(messages: LocaleMessages, locale: Locale): HTMLElement {
  const section = document.createElement("section");
  section.className = "page simple";

  const title = document.createElement("h1");
  title.textContent = messages.create.title;
  section.appendChild(title);

  const description = document.createElement("p");
  description.className = "muted";
  description.textContent = messages.create.description;
  section.appendChild(description);

  const stepper = renderCreateStepper(messages, locale);
  section.appendChild(stepper);

  return section;
}
