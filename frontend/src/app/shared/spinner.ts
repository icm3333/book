import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Indeterminate spinner. Sized and coloured by the host's classes (`size-*`, `text-*`).
 *
 * Decorative by default, for use inside buttons that already say what is happening. Give it a
 * `label` when it stands alone and it becomes a polite live region.
 */
@Component({
  selector: 'app-spinner',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'inline-block shrink-0',
    '[attr.role]': 'label() ? "status" : null',
    '[attr.aria-hidden]': 'label() ? null : "true"',
  },
  template: `
    <svg class="block size-full motion-safe:animate-spin" viewBox="0 0 24 24" fill="none">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z" />
    </svg>
    @if (label(); as text) {
      <span class="sr-only">{{ text }}</span>
    }
  `,
})
export class Spinner {
  readonly label = input<string>();
}
