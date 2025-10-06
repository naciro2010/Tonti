import { Dialog, Transition } from '@headlessui/react';
import type { ReactNode } from 'react';
import { Fragment } from 'react';
import { Button } from './Button';

type ModalProps = {
  open: boolean;
  onClose: () => void;
  title?: string;
  description?: string;
  children: ReactNode;
  primaryAction?: {
    label: string;
    onClick: () => void;
  };
};

export function Modal({ open, onClose, title, description, children, primaryAction }: ModalProps) {
  return (
    <Transition show={open} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-200"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-150"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/70" aria-hidden />
        </Transition.Child>

        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-200"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-150"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-xl overflow-hidden rounded-2xl bg-secondary p-6 text-left shadow-xl">
                <div className="space-y-4">
                  {title ? (
                    <Dialog.Title className="text-lg font-semibold text-white">{title}</Dialog.Title>
                  ) : null}
                  {description ? <p className="text-sm text-white/70">{description}</p> : null}
                  <div>{children}</div>
                  {primaryAction ? (
                    <div className="flex justify-end">
                      <Button onClick={primaryAction.onClick}>{primaryAction.label}</Button>
                    </div>
                  ) : null}
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
}
