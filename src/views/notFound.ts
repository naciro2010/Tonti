import type { Locale } from "@lib/i18n";
import type { LocaleMessages } from "@lib/i18n";

export function renderNotFound(messages: LocaleMessages, locale: Locale): HTMLElement {
  const section = document.createElement("section");
  section.className = "page simple";

  const title = document.createElement("h1");
  title.textContent = messages.notFound.title;
  section.appendChild(title);

  const description = document.createElement("p");
  description.className = "muted";
  description.textContent = messages.notFound.description;
  section.appendChild(description);

  const link = document.createElement("a");
  const href = locale === "fr" ? "/" : "/ar";
  link.href = href;
  link.setAttribute("data-route", href);
  link.className = "button primary";
  link.textContent = messages.notFound.back;
  section.appendChild(link);

  return section;
}
