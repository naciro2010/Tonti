import { reactive, toRaw } from 'vue';
import type { ZodSchema } from 'zod';

export interface FormState<T> {
  values: T;
  errors: Partial<Record<keyof T, string>>;
}

export function useZodForm<T extends Record<string, unknown>>(schema: ZodSchema<T>, initial: T) {
  const state = reactive<FormState<T>>({
    values: structuredClone(initial),
    errors: {},
  });

  function validate(field?: keyof T) {
    const result = schema.safeParse(toRaw(state.values));

    state.errors = {};

    if (!result.success) {
      result.error.errors.forEach((issue) => {
        const path = issue.path[0] as keyof T | undefined;
        if (path && (!field || field === path)) {
          state.errors[path] = issue.message;
        }
      });
    }

    if (field) {
      return !state.errors[field];
    }

    return result.success;
  }

  function reset(next?: Partial<T>) {
    state.values = Object.assign(structuredClone(initial), next);
    state.errors = {};
  }

  async function submit(handler: (values: T) => Promise<void> | void) {
    const isValid = validate();
    if (!isValid) {
      return false;
    }

    await handler(toRaw(state.values));
    return true;
  }

  return {
    state,
    validate,
    submit,
    reset,
  };
}
