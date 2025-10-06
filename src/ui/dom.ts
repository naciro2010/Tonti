export type ElementOptions = {
  classes?: string[];
  text?: string;
};

export function el<K extends keyof HTMLElementTagNameMap>(tag: K, options: ElementOptions = {}): HTMLElementTagNameMap[K] {
  const element = document.createElement(tag);
  if (options.classes) {
    element.className = options.classes.join(" ");
  }
  if (options.text) {
    element.textContent = options.text;
  }
  return element;
}

export function clearChildren(node: HTMLElement) {
  while (node.firstChild) {
    node.removeChild(node.firstChild);
  }
}
