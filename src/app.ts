import { state, subscribe, setPath, initHistory, setLocale } from "@core/state";
import { messages, LOCALES, type Locale, type LocaleMessages } from "@lib/i18n";
import { configureToasts } from "@ui/toast";
import { clearChildren } from "@ui/dom";
import { renderHome } from "@views/home";
import { renderHow } from "@views/how";
import { renderFaq } from "@views/faq";
import { renderHelp } from "@views/help";
import { renderLegalPage } from "@views/legalPage";
import { renderLogin } from "@views/login";
import { renderCagnotte } from "@views/cagnotte";
import { renderCreate } from "@views/create";
import { renderNotFound } from "@views/notFound";

const root = document.getElementById("app");
if (!root) {
  throw new Error("Unable to locate #app container");
}

const shell = document.createElement("div");
shell.className = "shell";
root.appendChild(shell);

const header = document.createElement("header");
header.className = "site-header";
shell.appendChild(header);

const logo = document.createElement("a");
logo.className = "logo";
logo.textContent = "Tonti";
header.appendChild(logo);

const nav = document.createElement("nav");
nav.className = "site-nav";
const navList = document.createElement("ul");
navList.className = "nav-list";
nav.appendChild(navList);
header.appendChild(nav);

const localeSwitch = document.createElement("div");
localeSwitch.className = "locale-switch";
header.appendChild(localeSwitch);

const main = document.createElement("main");
main.className = "main";
shell.appendChild(main);

const footer = document.createElement("footer");
footer.className = "site-footer";
shell.appendChild(footer);

const footerLinks = document.createElement("div");
footerLinks.className = "footer-links";
footer.appendChild(footerLinks);

const footerNote = document.createElement("p");
footerNote.className = "footer-note";
footer.appendChild(footerNote);

const navConfig: { key: keyof LocaleMessages["nav"]; relativePath: string; element: HTMLAnchorElement }[] = [
  { key: "home", relativePath: "/", element: document.createElement("a") },
  { key: "create", relativePath: "/cagnotte/creer", element: document.createElement("a") },
  { key: "how", relativePath: "/comment-ca-marche", element: document.createElement("a") },
  { key: "faq", relativePath: "/faq", element: document.createElement("a") },
  { key: "help", relativePath: "/aide", element: document.createElement("a") },
  { key: "legal", relativePath: "/mentions-legales", element: document.createElement("a") }
];

for (const item of navConfig) {
  const listItem = document.createElement("li");
  item.element.setAttribute("data-route", item.relativePath);
  listItem.appendChild(item.element);
  navList.appendChild(listItem);
}

const footerConfig: { key: keyof LocaleMessages["footer"]; relativePath: string; element: HTMLAnchorElement }[] = [
  { key: "legal", relativePath: "/mentions-legales", element: document.createElement("a") },
  { key: "privacy", relativePath: "/politique-confidentialite", element: document.createElement("a") },
  { key: "terms", relativePath: "/cgu", element: document.createElement("a") }
];

for (const item of footerConfig) {
  item.element.setAttribute("data-route", item.relativePath);
  footerLinks.appendChild(item.element);
}

const localeButtons = new Map<Locale, HTMLButtonElement>();
(["fr", "ar"] as const).forEach((locale) => {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "locale-button";
  button.dataset.switchLocale = locale;
  button.setAttribute("aria-label", LOCALES[locale].label);
  localeButtons.set(locale, button);
  localeSwitch.appendChild(button);
});

function parsePath(pathname: string): { locale: Locale; relativePath: string } {
  if (pathname === "/ar" || pathname.startsWith("/ar/")) {
    const remainder = pathname.slice(3) || "/";
    return { locale: "ar", relativePath: remainder.startsWith("/") ? remainder : `/${remainder}` };
  }
  return { locale: "fr", relativePath: pathname || "/" };
}

function localizePath(locale: Locale, relativePath: string): string {
  const normalized = relativePath.startsWith("/") ? relativePath : `/${relativePath}`;
  if (locale === "ar") {
    return normalized === "/" ? "/ar" : `/ar${normalized}`;
  }
  return normalized === "/" ? "/" : normalized;
}

function updateHeader(locale: Locale, localeMessages: LocaleMessages, relativePath: string) {
  logo.textContent = locale === "ar" ? "تونتي" : "Tonti";
  const localizedHome = localizePath(locale, "/");
  logo.href = localizedHome;
  logo.setAttribute("data-route", localizedHome);

  navConfig.forEach((item) => {
    const text = localeMessages.nav[item.key];
    item.element.textContent = text;
    const href = localizePath(locale, item.relativePath);
    item.element.href = href;
    item.element.dataset.route = href;
    if (relativePath === item.relativePath) {
      item.element.setAttribute("aria-current", "page");
    } else {
      item.element.removeAttribute("aria-current");
    }
  });

  localeButtons.forEach((button, key) => {
    button.textContent = LOCALES[key].label;
    button.setAttribute("aria-label", LOCALES[key].label);
    button.ariaPressed = key === locale ? "true" : "false";
    if (key === locale) {
      button.setAttribute("aria-current", "true");
    } else {
      button.removeAttribute("aria-current");
    }
  });
}

function updateFooter(locale: Locale, localeMessages: LocaleMessages) {
  footerConfig.forEach((item) => {
    item.element.textContent = localeMessages.footer[item.key];
    const href = localizePath(locale, item.relativePath);
    item.element.href = href;
    item.element.dataset.route = href;
  });
  const year = new Date().getFullYear();
  footerNote.textContent = locale === "ar" ? `${year} © تونتي` : `${year} © Tonti`;
}

function renderRoute(locale: Locale, localeMessages: LocaleMessages, relativePath: string): HTMLElement {
  if (relativePath === "/") {
    return renderHome(localeMessages, locale);
  }
  if (relativePath === "/comment-ca-marche") {
    return renderHow(localeMessages);
  }
  if (relativePath === "/faq") {
    return renderFaq(localeMessages);
  }
  if (relativePath === "/aide") {
    return renderHelp(localeMessages);
  }
  if (relativePath === "/cagnotte/creer") {
    return renderCreate(localeMessages, locale);
  }
  if (relativePath.startsWith("/cagnotte/")) {
    const slug = relativePath.split("/")[2] ?? "";
    return renderCagnotte(localeMessages, locale, slug);
  }
  if (relativePath === "/auth/login") {
    return renderLogin(localeMessages);
  }
  if (relativePath === "/mentions-legales") {
    return renderLegalPage(localeMessages, "legal");
  }
  if (relativePath === "/politique-confidentialite") {
    return renderLegalPage(localeMessages, "privacy");
  }
  if (relativePath === "/cgu") {
    return renderLegalPage(localeMessages, "terms");
  }
  return renderNotFound(localeMessages, locale);
}

function computePageTitle(localeMessages: LocaleMessages, relativePath: string): string | null {
  if (relativePath === "/") return localeMessages.nav.home;
  if (relativePath === "/comment-ca-marche") return localeMessages.nav.how;
  if (relativePath === "/faq") return localeMessages.nav.faq;
  if (relativePath === "/aide") return localeMessages.nav.help;
  if (relativePath === "/cagnotte/creer") return localeMessages.nav.create;
  if (relativePath.startsWith("/cagnotte/")) return localeMessages.cagnotte.detailsTitle;
  if (relativePath === "/auth/login") return localeMessages.login.title;
  if (relativePath === "/mentions-legales") return localeMessages.footer.legal;
  if (relativePath === "/politique-confidentialite") return localeMessages.footer.privacy;
  if (relativePath === "/cgu") return localeMessages.footer.terms;
  return localeMessages.notFound.title;
}

function renderApp() {
  const parsed = parsePath(state.path);
  if (state.locale !== parsed.locale) {
    setLocale(parsed.locale);
    return;
  }

  const localeMessages = messages[state.locale];
  configureToasts(localeMessages);
  updateHeader(state.locale, localeMessages, parsed.relativePath);
  updateFooter(state.locale, localeMessages);

  clearChildren(main);
  const view = renderRoute(state.locale, localeMessages, parsed.relativePath);
  main.appendChild(view);

  const baseTitle = state.locale === "fr" ? "Tonti" : "تونتي";
  const suffix = computePageTitle(localeMessages, parsed.relativePath);
  document.title = suffix ? `${baseTitle} – ${suffix}` : baseTitle;
}

subscribe(renderApp);
initHistory();

history.replaceState({ path: window.location.pathname }, "", window.location.pathname);
state.path = window.location.pathname;
renderApp();

function handleDocumentClick(event: MouseEvent) {
  const target = event.target as HTMLElement;
  const localeButton = target.closest<HTMLButtonElement>("[data-switch-locale]");
  if (localeButton) {
    event.preventDefault();
    const next = localeButton.dataset.switchLocale as Locale | undefined;
    if (!next) return;
    const parsed = parsePath(state.path);
    if (parsed.locale === next) return;
    const nextPath = localizePath(next, parsed.relativePath);
    if (nextPath !== state.path) {
      setPath(nextPath);
    }
    return;
  }

  const anchor = target.closest<HTMLAnchorElement>("a[data-route]");
  if (anchor) {
    const route = anchor.dataset.route ?? anchor.getAttribute("href");
    if (!route) return;
    const url = new URL(route, window.location.origin);
    if (url.origin !== window.location.origin) {
      return;
    }
    event.preventDefault();
    if (url.pathname !== state.path) {
      setPath(url.pathname);
    }
  }
}

document.addEventListener("click", handleDocumentClick);
