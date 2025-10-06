import type { LocaleMessages } from "@lib/i18n";

export type ToastTone = "success" | "warning" | "danger";

export type ToastMessage = {
  id: string;
  title: string;
  description?: string;
  tone?: ToastTone;
};

const toasts: ToastMessage[] = [];
let container: HTMLElement | null = null;
let closeLabel = "Fermer";

function ensureContainer(): HTMLElement {
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    document.body.appendChild(container);
  }
  return container;
}

function render() {
  const target = ensureContainer();
  target.innerHTML = "";
  for (const toast of toasts) {
    const item = document.createElement("div");
    item.className = `toast ${toast.tone ?? ""}`.trim();

    const header = document.createElement("div");
    header.className = "toast-header";
    header.textContent = toast.title;
    item.appendChild(header);

    if (toast.description) {
      const body = document.createElement("p");
      body.className = "toast-body";
      body.textContent = toast.description;
      item.appendChild(body);
    }

    const button = document.createElement("button");
    button.className = "toast-close";
    button.textContent = closeLabel;
    button.addEventListener("click", () => dismiss(toast.id));
    item.appendChild(button);

    target.appendChild(item);
  }
}

export function showToast(message: ToastMessage, lifetime = 4000) {
  toasts.push(message);
  render();
  if (lifetime > 0) {
    window.setTimeout(() => dismiss(message.id), lifetime);
  }
}

export function dismiss(id: string) {
  const index = toasts.findIndex((toast) => toast.id === id);
  if (index >= 0) {
    toasts.splice(index, 1);
    render();
  }
}

export function configureToasts(messages: LocaleMessages) {
  closeLabel = messages.toastClose;
  render();
}
