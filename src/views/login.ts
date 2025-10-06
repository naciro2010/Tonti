import type { LocaleMessages } from "@lib/i18n";

export function renderLogin(messages: LocaleMessages): HTMLElement {
  const section = document.createElement("section");
  section.className = "page form";

  const title = document.createElement("h1");
  title.textContent = messages.login.title;
  section.appendChild(title);

  const form = document.createElement("form");
  form.className = "card";
  form.addEventListener("submit", (event) => {
    event.preventDefault();
  });

  const emailLabel = document.createElement("label");
  emailLabel.className = "input-field";
  emailLabel.textContent = messages.login.emailLabel;
  const emailInput = document.createElement("input");
  emailInput.type = "email";
  emailInput.required = true;
  emailInput.className = "input";
  emailLabel.appendChild(emailInput);
  form.appendChild(emailLabel);

  const passwordLabel = document.createElement("label");
  passwordLabel.className = "input-field";
  passwordLabel.textContent = messages.login.passwordLabel;
  const passwordInput = document.createElement("input");
  passwordInput.type = "password";
  passwordInput.required = true;
  passwordInput.className = "input";
  passwordLabel.appendChild(passwordInput);
  form.appendChild(passwordLabel);

  const button = document.createElement("button");
  button.type = "submit";
  button.className = "button primary full";
  button.textContent = messages.login.submit;
  form.appendChild(button);

  const note = document.createElement("p");
  note.className = "form-note";
  note.textContent = messages.login.socialNote;
  form.appendChild(note);

  section.appendChild(form);
  return section;
}
