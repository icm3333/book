import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ToastContainer } from './core/notifications/toast-container';

/** Toasts sit outside the shell so they also appear on the login and register pages. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastContainer],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <router-outlet />
    <app-toast-container />
  `,
})
export class App {}
