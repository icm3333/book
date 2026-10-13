import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { Icon, IconName } from './icon';

/** Friendly placeholder for empty lists and failed loads. Actions are projected as content. */
@Component({
  selector: 'app-empty-state',
  imports: [Icon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  template: `
    <div
      class="flex flex-col items-center rounded-2xl border border-dashed border-slate-300 bg-white/60 px-6 py-14 text-center motion-safe:animate-fade-in"
    >
      <span
        class="flex size-12 items-center justify-center rounded-full"
        [class]="tone() === 'danger' ? 'bg-red-50 text-red-600' : 'bg-brand-50 text-brand-600'"
      >
        <app-icon [name]="icon()" class="size-6" />
      </span>
      <h2 class="mt-4 text-base font-semibold text-slate-900">{{ heading() }}</h2>
      <p class="mt-1 max-w-sm text-sm text-slate-500">{{ message() }}</p>
      <div class="mt-6 flex flex-wrap justify-center gap-3 empty:hidden">
        <ng-content />
      </div>
    </div>
  `,
})
export class EmptyState {
  readonly icon = input.required<IconName>();
  readonly heading = input.required<string>();
  readonly message = input.required<string>();
  readonly tone = input<'neutral' | 'danger'>('neutral');
}
