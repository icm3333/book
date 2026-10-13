import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { Icon } from '../../shared/icon';

/** Centred card layout shared by the login and register pages. */
@Component({
  selector: 'app-auth-layout',
  imports: [Icon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="flex min-h-dvh flex-col items-center justify-center bg-linear-to-b from-brand-50 via-slate-50 to-slate-50 px-4 py-12">
      <div class="w-full max-w-sm motion-safe:animate-fade-in">
        <div class="mb-8 flex flex-col items-center text-center">
          <span class="flex size-12 items-center justify-center rounded-xl bg-brand-600 text-white shadow-lg shadow-brand-600/30">
            <app-icon name="book-open" class="size-6" />
          </span>
          <h1 class="mt-5 text-2xl font-semibold tracking-tight text-slate-900">{{ heading() }}</h1>
          <p class="mt-1.5 text-sm text-slate-500">{{ subheading() }}</p>
        </div>

        <div class="card p-6 sm:p-8">
          <ng-content />
        </div>

        <div class="mt-6 text-center text-sm text-slate-600">
          <ng-content select="[authFooter]" />
        </div>
      </div>
    </main>
  `,
})
export class AuthLayout {
  readonly heading = input.required<string>();
  readonly subheading = input.required<string>();
}
