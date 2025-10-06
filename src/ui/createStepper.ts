import { categories } from "@data/categories";
import type { Locale } from "@lib/i18n";
import type { LocaleMessages } from "@lib/i18n";
import { showToast } from "./toast";

export type StepKey = "description" | "illustration" | "visibility" | "amounts" | "invitations";

export type FormState = {
  titre: string;
  description: string;
  categorie: string;
  image: string;
  visibilite: string;
  objectifMAD: number;
  minimumDonation: number;
  message: string;
};

const STORAGE_KEY = "tonti-create-cagnotte";

const DEFAULT_STATE: FormState = {
  titre: "",
  description: "",
  categorie: "aid",
  image: "",
  visibilite: "publique",
  objectifMAD: 1000,
  minimumDonation: 50,
  message: ""
};

function loadState(defaultMessage: string): FormState {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return { ...DEFAULT_STATE, message: defaultMessage };
    }
    const parsed = JSON.parse(raw) as Partial<FormState>;
    return { ...DEFAULT_STATE, message: defaultMessage, ...parsed };
  } catch {
    return { ...DEFAULT_STATE, message: defaultMessage };
  }
}

function persist(state: FormState) {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function validateStep(step: StepKey, state: FormState, messages: LocaleMessages): string | null {
  switch (step) {
    case "description": {
      if (state.titre.trim().length < 6) return messages.create.toasts.errorDescription;
      if (state.description.trim().length < 20) return messages.create.toasts.errorDescription;
      return null;
    }
    case "amounts": {
      if (state.objectifMAD < 1000) return messages.create.toasts.errorDescription;
      if (state.minimumDonation < 10) return messages.create.toasts.errorDescription;
      return null;
    }
    case "invitations": {
      if (state.message.trim().length < 10) return messages.create.toasts.errorDescription;
      return null;
    }
    default:
      return null;
  }
}

function createRadio(option: { id: string; label: string; description: string }, value: string, onChange: (next: string) => void) {
  const wrapper = document.createElement("label");
  wrapper.className = "step-radio";

  const input = document.createElement("input");
  input.type = "radio";
  input.name = "visibilite";
  input.value = option.id;
  input.checked = option.id === value;
  input.addEventListener("change", () => onChange(option.id));
  wrapper.appendChild(input);

  const content = document.createElement("div");
  const title = document.createElement("p");
  title.className = "step-radio-title";
  title.textContent = option.label;
  content.appendChild(title);

  const description = document.createElement("p");
  description.className = "step-radio-description";
  description.textContent = option.description;
  content.appendChild(description);

  wrapper.appendChild(content);
  return wrapper;
}

export function renderCreateStepper(messages: LocaleMessages, locale: Locale): HTMLElement {
  const container = document.createElement("div");
  container.className = "stepper";

  let state = loadState(messages.create.invitationsStep.messagePlaceholder);
  let currentStep: StepKey = "description";

  const stepOrder: StepKey[] = ["description", "illustration", "visibility", "amounts", "invitations"];

  const header = document.createElement("ol");
  header.className = "stepper-header";
  container.appendChild(header);

  const content = document.createElement("div");
  content.className = "stepper-content";
  container.appendChild(content);

  const actions = document.createElement("div");
  actions.className = "stepper-actions";
  container.appendChild(actions);

  function updateHeader() {
    header.innerHTML = "";
    stepOrder.forEach((step, index) => {
      const item = document.createElement("li");
      item.className = "stepper-item";
      if (step === currentStep) item.classList.add("active");
      if (stepOrder.indexOf(currentStep) > index) item.classList.add("done");

      const number = document.createElement("span");
      number.className = "stepper-number";
      number.textContent = String(index + 1);
      item.appendChild(number);

      const text = document.createElement("div");
      const title = document.createElement("p");
      title.className = "stepper-label";
      title.textContent = messages.create.steps[index].label;
      text.appendChild(title);
      const description = document.createElement("p");
      description.className = "stepper-description";
      description.textContent = messages.create.steps[index].description;
      text.appendChild(description);

      item.appendChild(text);
      header.appendChild(item);
    });
  }

  function renderDescriptionStep() {
    content.innerHTML = "";

    const titleInput = document.createElement("input");
    titleInput.type = "text";
    titleInput.className = "input";
    titleInput.value = state.titre;
    titleInput.placeholder = messages.create.descriptionStep.titlePlaceholder;
    titleInput.addEventListener("input", (event) => {
      state.titre = (event.target as HTMLInputElement).value;
      persist(state);
    });

    const titleLabel = document.createElement("label");
    titleLabel.className = "input-field";
    titleLabel.textContent = messages.create.descriptionStep.titleLabel;
    titleLabel.appendChild(titleInput);
    content.appendChild(titleLabel);

    const descriptionLabel = document.createElement("label");
    descriptionLabel.className = "input-field";
    descriptionLabel.textContent = messages.create.descriptionStep.descriptionLabel;

    const descriptionArea = document.createElement("textarea");
    descriptionArea.className = "textarea";
    descriptionArea.value = state.description;
    descriptionArea.placeholder = messages.create.descriptionStep.descriptionPlaceholder;
    descriptionArea.addEventListener("input", (event) => {
      state.description = (event.target as HTMLTextAreaElement).value;
      persist(state);
    });
    descriptionLabel.appendChild(descriptionArea);
    content.appendChild(descriptionLabel);

    const selectLabel = document.createElement("label");
    selectLabel.className = "input-field";
    selectLabel.textContent = messages.create.descriptionStep.categoryLabel;

    const select = document.createElement("select");
    select.className = "select";
    categories.forEach((category) => {
      const option = document.createElement("option");
      option.value = category.id;
      option.textContent = category.label[locale];
      if (state.categorie === category.id) option.selected = true;
      select.appendChild(option);
    });
    select.addEventListener("change", (event) => {
      state.categorie = (event.target as HTMLSelectElement).value;
      persist(state);
    });
    selectLabel.appendChild(select);
    content.appendChild(selectLabel);
  }

  function renderIllustrationStep() {
    content.innerHTML = "";
    const info = document.createElement("p");
    info.className = "step-info";
    info.textContent = messages.create.illustrationStep.intro;
    content.appendChild(info);

    const placeholder = document.createElement("div");
    placeholder.className = "dropzone";
    placeholder.textContent = messages.create.illustrationStep.placeholder;
    content.appendChild(placeholder);
  }

  function renderVisibilityStep() {
    content.innerHTML = "";
    const info = document.createElement("p");
    info.className = "step-info";
    info.textContent = messages.create.visibilityStep.intro;
    content.appendChild(info);

    messages.create.visibilityStep.options.forEach((option) => {
      const radio = createRadio(option, state.visibilite, (next) => {
        state.visibilite = next;
        persist(state);
        renderVisibilityStep();
      });
      content.appendChild(radio);
    });
  }

  function renderAmountsStep() {
    content.innerHTML = "";

    const goalLabel = document.createElement("label");
    goalLabel.className = "input-field";
    goalLabel.textContent = messages.create.amountsStep.goalLabel;

    const goalInput = document.createElement("input");
    goalInput.type = "number";
    goalInput.className = "input";
    goalInput.value = String(state.objectifMAD);
    goalInput.min = "0";
    goalInput.addEventListener("input", (event) => {
      state.objectifMAD = Number((event.target as HTMLInputElement).value);
      persist(state);
    });
    goalLabel.appendChild(goalInput);
    content.appendChild(goalLabel);

    const minLabel = document.createElement("label");
    minLabel.className = "input-field";
    minLabel.textContent = messages.create.amountsStep.minLabel;

    const minInput = document.createElement("input");
    minInput.type = "number";
    minInput.className = "input";
    minInput.value = String(state.minimumDonation);
    minInput.min = "0";
    minInput.addEventListener("input", (event) => {
      state.minimumDonation = Number((event.target as HTMLInputElement).value);
      persist(state);
    });
    minLabel.appendChild(minInput);
    content.appendChild(minLabel);

    const note = document.createElement("p");
    note.className = "step-note";
    note.textContent = messages.create.amountsStep.feesNotice;
    content.appendChild(note);
  }

  function renderInvitationsStep() {
    content.innerHTML = "";

    const info = document.createElement("p");
    info.className = "step-info";
    info.textContent = messages.create.invitationsStep.intro;
    content.appendChild(info);

    const messageArea = document.createElement("textarea");
    messageArea.className = "textarea";
    messageArea.value = state.message;
    messageArea.placeholder = messages.create.invitationsStep.messagePlaceholder;
    messageArea.addEventListener("input", (event) => {
      state.message = (event.target as HTMLTextAreaElement).value;
      persist(state);
    });
    content.appendChild(messageArea);

    const note = document.createElement("p");
    note.className = "step-note";
    note.textContent = messages.create.invitationsStep.note;
    content.appendChild(note);
  }

  function renderContent() {
    switch (currentStep) {
      case "description":
        renderDescriptionStep();
        break;
      case "illustration":
        renderIllustrationStep();
        break;
      case "visibility":
        renderVisibilityStep();
        break;
      case "amounts":
        renderAmountsStep();
        break;
      case "invitations":
        renderInvitationsStep();
        break;
    }
  }

  function goToNext() {
    const error = validateStep(currentStep, state, messages);
    if (error) {
      showToast({ id: crypto.randomUUID(), title: messages.create.toasts.errorTitle, description: error, tone: "warning" });
      return;
    }
    const index = stepOrder.indexOf(currentStep);
    if (index === stepOrder.length - 1) {
      showToast({ id: crypto.randomUUID(), title: messages.create.toasts.successTitle, description: messages.create.toasts.successDescription, tone: "success" });
      return;
    }
    currentStep = stepOrder[index + 1];
    updateHeader();
    renderContent();
  }

  function skipStep() {
    const index = stepOrder.indexOf(currentStep);
    if (index < stepOrder.length - 1) {
      currentStep = stepOrder[index + 1];
      updateHeader();
      renderContent();
    }
  }

  const skipButton = document.createElement("button");
  skipButton.type = "button";
  skipButton.className = "button ghost";
  skipButton.textContent = messages.create.actions.skip;
  skipButton.addEventListener("click", skipStep);
  actions.appendChild(skipButton);

  const nextButton = document.createElement("button");
  nextButton.type = "button";
  nextButton.className = "button primary";
  nextButton.textContent = messages.create.actions.continue;
  nextButton.addEventListener("click", goToNext);
  actions.appendChild(nextButton);

  updateHeader();
  renderContent();

  return container;
}
