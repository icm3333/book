import { ChangeDetectionStrategy, Component, computed, input, linkedSignal } from '@angular/core';

const GRADIENTS = [
  'from-indigo-500 to-violet-600',
  'from-sky-500 to-cyan-600',
  'from-emerald-500 to-teal-600',
  'from-amber-500 to-orange-600',
  'from-rose-500 to-pink-600',
  'from-fuchsia-500 to-purple-600',
] as const;

/**
 * Shows the cover image, or a deterministic coloured placeholder with the title's initials when
 * there is no image or it fails to load. The same title always gets the same colour.
 */
@Component({
  selector: 'app-book-cover',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  template: `
    @if (!failed() && src(); as url) {
      <img
        [src]="url"
        [alt]="'Cover of ' + title()"
        loading="lazy"
        decoding="async"
        class="aspect-[2/3] w-full rounded-md bg-slate-100 object-cover shadow-sm ring-1 ring-black/5"
        (error)="failed.set(true)"
      />
    } @else {
      <div
        role="img"
        [attr.aria-label]="'No cover available for ' + title()"
        class="flex aspect-[2/3] w-full items-center justify-center rounded-md bg-linear-to-br text-white shadow-sm ring-1 ring-black/5"
        [class]="gradient()"
      >
        <span class="text-lg font-semibold tracking-wide opacity-90" aria-hidden="true">{{ initials() }}</span>
      </div>
    }
  `,
})
export class BookCover {
  readonly src = input<string | null>(null);
  readonly title = input.required<string>();

  /** Resets whenever a new source arrives, so a broken image doesn't stick to the next one. */
  protected readonly failed = linkedSignal({ source: this.src, computation: () => false });

  protected readonly initials = computed(() =>
    this.title()
      .split(/\s+/)
      .filter((word) => /[\p{L}\p{N}]/u.test(word))
      .slice(0, 2)
      .map((word) => word.charAt(0).toUpperCase())
      .join(''),
  );

  protected readonly gradient = computed(() => {
    let hash = 0;
    for (const char of this.title()) {
      hash = (hash * 31 + char.charCodeAt(0)) | 0;
    }
    return GRADIENTS[Math.abs(hash) % GRADIENTS.length];
  });
}
