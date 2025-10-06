import { cagnottes } from "@data/cagnottes";
import { formatMAD } from "@lib/currency";
import { daysRemaining, formatRelative } from "@lib/date";
import type { Locale } from "@lib/i18n";
import type { LocaleMessages } from "@lib/i18n";

export function renderCagnotte(messages: LocaleMessages, locale: Locale, slug: string): HTMLElement {
  const section = document.createElement("section");
  section.className = "page cagnotte";

  const cagnotte = cagnottes.find((item) => item.id === slug);
  if (!cagnotte) {
    const title = document.createElement("h1");
    title.textContent = messages.cagnotte.notFoundTitle;
    section.appendChild(title);
    const paragraph = document.createElement("p");
    paragraph.textContent = messages.cagnotte.notFoundDescription;
    section.appendChild(paragraph);
    const link = document.createElement("a");
    const url = locale === "fr" ? "/" : "/ar";
    link.href = url;
    link.setAttribute("data-route", url);
    link.className = "button primary";
    link.textContent = messages.cagnotte.backHome;
    section.appendChild(link);
    return section;
  }

  const layout = document.createElement("div");
  layout.className = "cagnotte-layout";

  const main = document.createElement("article");
  main.className = "cagnotte-main";

  const header = document.createElement("div");
  header.className = "cagnotte-header";
  const title = document.createElement("h1");
  title.textContent = cagnotte.titre;
  header.appendChild(title);
  const organiser = document.createElement("p");
  organiser.className = "muted";
  organiser.textContent = `${messages.cagnotte.organizerPrefix} ${cagnotte.organisateur.nom}`;
  header.appendChild(organiser);
  main.appendChild(header);

  const description = document.createElement("p");
  description.className = "cagnotte-text";
  description.textContent = cagnotte.description;
  main.appendChild(description);

  const widget = document.createElement("div");
  widget.className = "cagnotte-widget";
  const widgetTitle = document.createElement("h2");
  widgetTitle.textContent = messages.cagnotte.progressTitle;
  widget.appendChild(widgetTitle);
  const progress = document.createElement("div");
  progress.className = "progress";
  const progressBar = document.createElement("div");
  progressBar.className = "progress-bar";
  const percentage = Math.min(Math.round((cagnotte.collecteMAD / cagnotte.objectifMAD) * 100), 100);
  progressBar.style.width = `${percentage}%`;
  progress.appendChild(progressBar);
  widget.appendChild(progress);

  const stats = document.createElement("div");
  stats.className = "widget-stats";

  const collected = document.createElement("div");
  const collectedLabel = document.createElement("span");
  collectedLabel.textContent = messages.cagnotte.collectedLabel;
  const collectedValue = document.createElement("strong");
  collectedValue.textContent = formatMAD(cagnotte.collecteMAD);
  collected.appendChild(collectedLabel);
  collected.appendChild(collectedValue);
  stats.appendChild(collected);

  const goal = document.createElement("div");
  const goalLabel = document.createElement("span");
  goalLabel.textContent = messages.cagnotte.goalLabel;
  const goalValue = document.createElement("strong");
  goalValue.textContent = formatMAD(cagnotte.objectifMAD);
  goal.appendChild(goalLabel);
  goal.appendChild(goalValue);
  stats.appendChild(goal);

  const remaining = document.createElement("div");
  const remainingLabel = document.createElement("span");
  remainingLabel.textContent = messages.cagnotte.daysLeftLabel;
  const remainingValue = document.createElement("strong");
  remainingValue.textContent = String(daysRemaining(cagnotte.dateFin));
  remaining.appendChild(remainingLabel);
  remaining.appendChild(remainingValue);
  stats.appendChild(remaining);
  widget.appendChild(stats);

  const participate = document.createElement("button");
  participate.className = "button primary full";
  participate.textContent = messages.cagnotte.participateCta;
  widget.appendChild(participate);

  main.appendChild(widget);

  const participants = document.createElement("section");
  participants.className = "card";
  const participantsHeader = document.createElement("header");
  participantsHeader.className = "card-header";
  const participantsTitle = document.createElement("h2");
  participantsTitle.textContent = messages.cagnotte.recentParticipants;
  participantsHeader.appendChild(participantsTitle);
  const count = document.createElement("span");
  count.className = "muted";
  count.textContent = messages.cagnotte.participantsCount.replace("{count}", String(cagnotte.participants));
  participantsHeader.appendChild(count);
  participants.appendChild(participantsHeader);

  const list = document.createElement("div");
  list.className = "contribution-list";
  cagnotte.historique.forEach((contribution) => {
    const item = document.createElement("article");
    item.className = "contribution";
    const headerRow = document.createElement("div");
    headerRow.className = "contribution-header";
    const name = document.createElement("span");
    name.textContent = contribution.nom;
    headerRow.appendChild(name);
    const amount = document.createElement("strong");
    amount.textContent = formatMAD(contribution.montantMAD);
    headerRow.appendChild(amount);
    item.appendChild(headerRow);
    const time = document.createElement("p");
    time.className = "muted";
    time.textContent = formatRelative(contribution.date, locale);
    item.appendChild(time);
    list.appendChild(item);
  });
  participants.appendChild(list);
  main.appendChild(participants);

  layout.appendChild(main);

  const aside = document.createElement("aside");
  aside.className = "cagnotte-aside";

  const details = document.createElement("div");
  details.className = "card";
  const detailsTitle = document.createElement("h2");
  detailsTitle.textContent = messages.cagnotte.detailsTitle;
  details.appendChild(detailsTitle);

  const dl = document.createElement("dl");
  dl.className = "details";

  const beneficiary = document.createElement("div");
  const beneficiaryLabel = document.createElement("dt");
  beneficiaryLabel.textContent = messages.cagnotte.beneficiaryLabel;
  const beneficiaryValue = document.createElement("dd");
  beneficiaryValue.textContent = cagnotte.beneficiaire.nom;
  beneficiary.appendChild(beneficiaryLabel);
  beneficiary.appendChild(beneficiaryValue);
  dl.appendChild(beneficiary);

  const visibility = document.createElement("div");
  const visibilityLabel = document.createElement("dt");
  visibilityLabel.textContent = messages.cagnotte.visibilityLabel;
  const visibilityValue = document.createElement("dd");
  const visibilityText = messages.cagnotte.visibilityMap[cagnotte.visibilite] ?? cagnotte.visibilite;
  visibilityValue.textContent = visibilityText;
  visibility.appendChild(visibilityLabel);
  visibility.appendChild(visibilityValue);
  dl.appendChild(visibility);

  const status = document.createElement("div");
  const statusLabel = document.createElement("dt");
  statusLabel.textContent = messages.cagnotte.statusLabel;
  const statusValue = document.createElement("dd");
  const statusText = messages.cagnotte.statusMap[cagnotte.statut] ?? cagnotte.statut;
  statusValue.textContent = statusText;
  status.appendChild(statusLabel);
  status.appendChild(statusValue);
  dl.appendChild(status);
  details.appendChild(dl);

  const note = document.createElement("p");
  note.className = "muted";
  note.textContent = messages.cagnotte.paymentNote;
  details.appendChild(note);
  aside.appendChild(details);

  const loginCard = document.createElement("div");
  loginCard.className = "card";
  const loginTitle = document.createElement("h3");
  loginTitle.textContent = messages.cagnotte.loginTitle;
  loginCard.appendChild(loginTitle);
  const loginDescription = document.createElement("p");
  loginDescription.className = "muted";
  loginDescription.textContent = messages.cagnotte.loginDescription;
  loginCard.appendChild(loginDescription);
  const loginLink = document.createElement("a");
  const loginUrl = locale === "fr" ? "/auth/login" : "/ar/auth/login";
  loginLink.href = loginUrl;
  loginLink.setAttribute("data-route", loginUrl);
  loginLink.className = "button secondary full";
  loginLink.textContent = messages.cagnotte.loginCta;
  loginCard.appendChild(loginLink);
  aside.appendChild(loginCard);

  layout.appendChild(aside);
  section.appendChild(layout);

  return section;
}
