import { LOCALES, type Locale } from "@lib/i18n";

type Listener = () => void;

type AppState = {
  locale: Locale;
  path: string;
};

const listeners: Listener[] = [];

export const state: AppState = {
  locale: "fr",
  path: "/"
};

export function subscribe(listener: Listener): () => void {
  listeners.push(listener);
  return () => {
    const index = listeners.indexOf(listener);
    if (index >= 0) {
      listeners.splice(index, 1);
    }
  };
}

function notify() {
  for (const listener of listeners) {
    listener();
  }
}

export function setLocale(next: Locale) {
  if (state.locale === next) return;
  state.locale = next;
  const metadata = LOCALES[next];
  const html = document.documentElement;
  html.lang = next;
  html.dir = metadata.dir;
  if (next === "ar") {
    html.classList.add("rtl");
  } else {
    html.classList.remove("rtl");
  }
  notify();
}

export function setPath(path: string, replace = false) {
  if (state.path === path) return;
  state.path = path;
  if (replace) {
    history.replaceState({ path }, "", path);
  } else {
    history.pushState({ path }, "", path);
  }
  notify();
}

export function initHistory() {
  window.addEventListener("popstate", (event) => {
    const nextPath = typeof event.state?.path === "string" ? event.state.path : window.location.pathname;
    state.path = nextPath;
    notify();
  });
}
