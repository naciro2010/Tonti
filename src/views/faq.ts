import type { LocaleMessages } from "@lib/i18n";

export function renderFaq(messages: LocaleMessages): HTMLElement {
  const section = document.createElement("section");
  section.className = "page simple";

  const title = document.createElement("h1");
  title.textContent = messages.faq.title;
  section.appendChild(title);

  messages.faq.items.forEach((item) => {
    const details = document.createElement("details");
    if (item.defaultOpen) details.open = true;
    const summary = document.createElement("summary");
    summary.textContent = item.question;
    details.appendChild(summary);
    const paragraph = document.createElement("p");
    paragraph.textContent = item.answer;
    details.appendChild(paragraph);
    section.appendChild(details);
  });

  return section;
}
