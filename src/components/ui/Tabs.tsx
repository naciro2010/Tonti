import { Tab } from '@headlessui/react';
import type { ReactNode } from 'react';
import { twMerge } from 'tailwind-merge';

type TabItem = {
  id: string;
  title: string;
  content: ReactNode;
};

type TabsProps = {
  items: TabItem[];
  defaultIndex?: number;
};

export function Tabs({ items, defaultIndex = 0 }: TabsProps) {
  return (
    <Tab.Group defaultIndex={defaultIndex}>
      <Tab.List className="flex space-x-2 rounded-2xl border border-white/10 bg-white/[0.04] p-1">
        {items.map((item) => (
          <Tab
            key={item.id}
            className={({ selected }) =>
              twMerge(
                'w-full rounded-xl px-3 py-2 text-sm font-semibold transition focus:outline-none focus-visible:ring-2 focus-visible:ring-primary',
                selected ? 'bg-primary text-secondary shadow' : 'text-white/70 hover:text-white',
              )
            }
          >
            {item.title}
          </Tab>
        ))}
      </Tab.List>
      <Tab.Panels className="mt-4">
        {items.map((item) => (
          <Tab.Panel key={item.id} className="rounded-3xl border border-white/10 bg-white/[0.03] p-4">
            {item.content}
          </Tab.Panel>
        ))}
      </Tab.Panels>
    </Tab.Group>
  );
}
