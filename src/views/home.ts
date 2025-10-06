import { categories } from "@data/categories";
import { cagnottes } from "@data/cagnottes";
import { formatMAD } from "@lib/currency";
import { daysRemaining } from "@lib/date";
import type { Locale } from "@lib/i18n";
import type { LocaleMessages } from "@lib/i18n";

export function renderHome(messages: LocaleMessages, locale: Locale): HTMLElement {
  const section = document.createElement("section");
  section.className = "page home";

  const hero = document.createElement("div");
  hero.className = "hero";
  const heroText = document.createElement("div");
  heroText.className = "hero-text";

  const title = document.createElement("h1");
  title.textContent = messages.home.heroTitle;
  heroText.appendChild(title);

  const subtitle = document.createElement("p");
  subtitle.textContent = messages.home.heroSubtitle;
  heroText.appendChild(subtitle);

  const cta = document.createElement("a");
  cta.textContent = messages.home.heroCta;
  cta.href = locale === "fr" ? "/cagnotte/creer" : "/ar/cagnotte/creer";
  cta.setAttribute("data-route", cta.href);
  cta.className = "button primary";
  heroText.appendChild(cta);

  hero.appendChild(heroText);

  const legal = document.createElement("div");
  legal.className = "hero-legal";
  const legalTitle = document.createElement("p");
  legalTitle.className = "hero-legal-title";
  legalTitle.textContent = messages.home.legalHeading;
  legal.appendChild(legalTitle);
  const legalText = document.createElement("p");
  legalText.textContent = messages.home.legalDescription;
  legal.appendChild(legalText);
  hero.appendChild(legal);

  section.appendChild(hero);

  const categoriesSection = document.createElement("div");
  categoriesSection.className = "card-section";
  const categoriesTitle = document.createElement("h2");
  categoriesTitle.textContent = messages.home.categoriesTitle;
  categoriesSection.appendChild(categoriesTitle);

  const categoriesGrid = document.createElement("div");
  categoriesGrid.className = "grid three";
  categories.forEach((category) => {
    const card = document.createElement("div");
    card.className = "category-card";
    const icon = document.createElement("span");
    icon.className = "category-icon";
    icon.textContent = category.icon;
    card.appendChild(icon);
    const label = document.createElement("span");
    label.textContent = category.label[locale];
    card.appendChild(label);
    categoriesGrid.appendChild(card);
  });
  categoriesSection.appendChild(categoriesGrid);
  section.appendChild(categoriesSection);

  const featuredSection = document.createElement("div");
  featuredSection.className = "card-section";
  const featuredHeader = document.createElement("div");
  featuredHeader.className = "section-header";
  const featuredTitle = document.createElement("h2");
  featuredTitle.textContent = messages.home.featuredTitle;
  featuredHeader.appendChild(featuredTitle);
  const featuredLink = document.createElement("a");
  featuredLink.href = locale === "fr" ? "/cagnotte/creer" : "/ar/cagnotte/creer";
  featuredLink.setAttribute("data-route", featuredLink.href);
  featuredLink.textContent = messages.home.featuredCta;
  featuredHeader.appendChild(featuredLink);
  featuredSection.appendChild(featuredHeader);

  const featuredGrid = document.createElement("div");
  featuredGrid.className = "grid two";

  cagnottes.forEach((cagnotte) => {
    const article = document.createElement("article");
    article.className = "cagnotte-card";

    const header = document.createElement("div");
    header.className = "cagnotte-card-header";
    const h3 = document.createElement("h3");
    h3.textContent = cagnotte.titre;
    header.appendChild(h3);
    const badge = document.createElement("span");
    badge.className = "badge";
    badge.textContent = cagnotte.categorie;
    header.appendChild(badge);
    article.appendChild(header);

    const description = document.createElement("p");
    description.className = "cagnotte-description";
    description.textContent = cagnotte.description;
    article.appendChild(description);

    const progress = document.createElement("div");
    progress.className = "progress";
    const progressBar = document.createElement("div");
    progressBar.className = "progress-bar";
    const percentage = Math.min(Math.round((cagnotte.collecteMAD / cagnotte.objectifMAD) * 100), 100);
    progressBar.style.width = `${percentage}%`;
    progress.appendChild(progressBar);
    article.appendChild(progress);

    const stats = document.createElement("dl");
    stats.className = "stats";

    const collected = document.createElement("div");
    const collectedLabel = document.createElement("dt");
    collectedLabel.textContent = messages.home.cardCollected;
    const collectedValue = document.createElement("dd");
    collectedValue.textContent = formatMAD(cagnotte.collecteMAD);
    collected.appendChild(collectedLabel);
    collected.appendChild(collectedValue);
    stats.appendChild(collected);

    const goal = document.createElement("div");
    const goalLabel = document.createElement("dt");
    goalLabel.textContent = messages.home.cardGoal;
    const goalValue = document.createElement("dd");
    goalValue.textContent = formatMAD(cagnotte.objectifMAD);
    goal.appendChild(goalLabel);
    goal.appendChild(goalValue);
    stats.appendChild(goal);

    const remaining = document.createElement("div");
    const remainingLabel = document.createElement("dt");
    remainingLabel.textContent = messages.home.cardDaysLeft;
    const remainingValue = document.createElement("dd");
    remainingValue.textContent = String(daysRemaining(cagnotte.dateFin));
    remaining.appendChild(remainingLabel);
    remaining.appendChild(remainingValue);
    stats.appendChild(remaining);

    article.appendChild(stats);

    const link = document.createElement("a");
    link.className = "button secondary";
    const url = locale === "fr" ? `/cagnotte/${cagnotte.id}` : `/ar/cagnotte/${cagnotte.id}`;
    link.href = url;
    link.setAttribute("data-route", url);
    link.textContent = messages.home.cardView;
    article.appendChild(link);

    featuredGrid.appendChild(article);
  });

  featuredSection.appendChild(featuredGrid);
  section.appendChild(featuredSection);

  return section;
}
