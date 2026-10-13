import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { Icon, IconName } from '../../shared/icon';
import { NotificationService, ToastKind } from './notification.service';

const TOAST_STYLES: Record<ToastKind, { icon: IconName; iconClass: string }> = {
  success: { icon: 'check-circle', iconClass: 'text-emerald-600' },
  error: { icon: 'alert-circle', iconClass: 'text-red-600' },
  info: { icon: 'info', iconClass: 'text-brand-600' },
};

@Component({
  selector: 'app-toast-container',
  imports: [Icon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="pointer-events-none fixed inset-x-0 top-0 z-50 flex flex-col items-center gap-2 p-4 sm:items-end sm:p-6"
      aria-live="polite"
      aria-atomic="false"
    >
      @for (toast of notifications.toasts(); track toast.id) {
        <div
          class="pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl bg-white p-4 shadow-lg ring-1 ring-slate-900/10 motion-safe:animate-toast-in"
        >
          <app-icon [name]="styles[toast.kind].icon" class="mt-0.5 size-5" [class]="styles[toast.kind].iconClass" />
          <p class="flex-1 text-sm text-slate-700">{{ toast.message }}</p>
          <button
            type="button"
            class="-m-1 rounded-md p-1 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
            aria-label="Dismiss notification"
            (click)="notifications.dismiss(toast.id)"
          >
            <app-icon name="x" class="size-4" />
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastContainer {
  protected readonly notifications = inject(NotificationService);
  protected readonly styles = TOAST_STYLES;
}
