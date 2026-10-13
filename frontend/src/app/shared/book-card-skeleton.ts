import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Placeholder with the same footprint as a book card, shown while a list first loads. */
@Component({
  selector: 'app-book-card-skeleton',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block', 'aria-hidden': 'true' },
  template: `
    <div class="card flex gap-4 p-4">
      <div class="aspect-[2/3] w-20 shrink-0 rounded-md bg-slate-200 motion-safe:animate-pulse sm:w-24"></div>
      <div class="flex-1 space-y-3 py-1">
        <div class="h-4 w-3/4 rounded bg-slate-200 motion-safe:animate-pulse"></div>
        <div class="h-3 w-1/2 rounded bg-slate-200 motion-safe:animate-pulse"></div>
        <div class="mt-6 h-1.5 w-full rounded-full bg-slate-200 motion-safe:animate-pulse"></div>
        <div class="flex gap-2 pt-3">
          <div class="h-8 w-28 rounded-lg bg-slate-200 motion-safe:animate-pulse"></div>
          <div class="ml-auto h-8 w-24 rounded-lg bg-slate-200 motion-safe:animate-pulse"></div>
        </div>
      </div>
    </div>
  `,
})
export class BookCardSkeleton {}
