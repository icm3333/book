import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type IconName =
  | 'alert-circle'
  | 'book-open'
  | 'check'
  | 'check-circle'
  | 'eye'
  | 'eye-off'
  | 'inbox'
  | 'info'
  | 'library'
  | 'log-out'
  | 'minus'
  | 'plus'
  | 'refresh'
  | 'search'
  | 'x';

/**
 * Outline icons in the Lucide style (24×24, 2px stroke, ISC-licensed path data), inlined so the
 * app ships only the icons it uses with no extra dependency.
 *
 * Size and colour come from the host's classes (`size-4 text-slate-500`). Icons are always
 * decorative; the surrounding control carries the accessible name.
 */
@Component({
  selector: 'app-icon',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'inline-block shrink-0', 'aria-hidden': 'true' },
  template: `
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      class="block size-full"
      focusable="false"
    >
      @switch (name()) {
        @case ('alert-circle') {
          <svg:circle cx="12" cy="12" r="10" />
          <svg:line x1="12" x2="12" y1="8" y2="12" />
          <svg:line x1="12" x2="12.01" y1="16" y2="16" />
        }
        @case ('book-open') {
          <svg:path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
          <svg:path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
        }
        @case ('check') {
          <svg:path d="M20 6 9 17l-5-5" />
        }
        @case ('check-circle') {
          <svg:circle cx="12" cy="12" r="10" />
          <svg:path d="m9 12 2 2 4-4" />
        }
        @case ('eye') {
          <svg:path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
          <svg:circle cx="12" cy="12" r="3" />
        }
        @case ('eye-off') {
          <svg:path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
          <svg:path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
          <svg:path d="M6.61 6.61A13.53 13.53 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
          <svg:line x1="2" x2="22" y1="2" y2="22" />
        }
        @case ('inbox') {
          <svg:polyline points="22 12 16 12 14 15 10 15 8 12 2 12" />
          <svg:path
            d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"
          />
        }
        @case ('info') {
          <svg:circle cx="12" cy="12" r="10" />
          <svg:path d="M12 16v-4" />
          <svg:path d="M12 8h.01" />
        }
        @case ('library') {
          <svg:path d="m16 6 4 14" />
          <svg:path d="M12 6v14" />
          <svg:path d="M8 8v12" />
          <svg:path d="M4 4v16" />
        }
        @case ('log-out') {
          <svg:path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <svg:polyline points="16 17 21 12 16 7" />
          <svg:line x1="21" x2="9" y1="12" y2="12" />
        }
        @case ('minus') {
          <svg:path d="M5 12h14" />
        }
        @case ('plus') {
          <svg:path d="M5 12h14" />
          <svg:path d="M12 5v14" />
        }
        @case ('refresh') {
          <svg:path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8" />
          <svg:path d="M21 3v5h-5" />
        }
        @case ('search') {
          <svg:circle cx="11" cy="11" r="8" />
          <svg:path d="m21 21-4.3-4.3" />
        }
        @case ('x') {
          <svg:path d="M18 6 6 18" />
          <svg:path d="m6 6 12 12" />
        }
      }
    </svg>
  `,
})
export class Icon {
  readonly name = input.required<IconName>();
}
