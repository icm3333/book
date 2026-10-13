import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  kind: ToastKind;
  message: string;
}

/** Most toasts visible at once; older ones are dropped so the stack never covers the page. */
const MAX_VISIBLE = 4;

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 0;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private readonly _toasts = signal<readonly Toast[]>([]);

  readonly toasts = this._toasts.asReadonly();

  success(message: string): void {
    this.push('success', message, 4000);
  }

  info(message: string): void {
    this.push('info', message, 4000);
  }

  /** Errors stay up longer because they usually need to be read, not just noticed. */
  error(message: string): void {
    this.push('error', message, 7000);
  }

  dismiss(id: number): void {
    clearTimeout(this.timers.get(id));
    this.timers.delete(id);
    this._toasts.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }

  private push(kind: ToastKind, message: string, timeoutMs: number): void {
    // Several failing requests often report the same problem; show it once.
    if (this._toasts().some((toast) => toast.kind === kind && toast.message === message)) {
      return;
    }

    const toast: Toast = { id: ++this.nextId, kind, message };
    this._toasts.update((toasts) => [...toasts, toast].slice(-MAX_VISIBLE));
    this.timers.set(
      toast.id,
      setTimeout(() => this.dismiss(toast.id), timeoutMs),
    );
  }
}
