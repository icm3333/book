import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/http/error-message';
import { NotificationService } from '../../core/notifications/notification.service';
import { Icon } from '../../shared/icon';
import { Spinner } from '../../shared/spinner';
import { AuthLayout } from './auth-layout';

type RegisterField = 'username' | 'email' | 'password';

export const USERNAME_MIN = 3;
export const USERNAME_MAX = 40;
export const PASSWORD_MIN = 8;

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink, AuthLayout, Icon, Spinner],
  templateUrl: './register-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  protected readonly passwordMin = PASSWORD_MIN;

  protected readonly form = inject(NonNullableFormBuilder).group({
    username: ['', [Validators.required, Validators.minLength(USERNAME_MIN), Validators.maxLength(USERNAME_MAX)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(PASSWORD_MIN)]],
  });

  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);
  protected readonly showPassword = signal(false);

  /** The message for a field, or null while it is untouched or valid. */
  protected errorFor(field: RegisterField): string | null {
    const control = this.form.controls[field];
    if (!control.touched || control.valid) {
      return null;
    }

    switch (field) {
      case 'username':
        if (control.hasError('required')) return 'Choose a username.';
        if (control.hasError('minlength')) return `Use at least ${USERNAME_MIN} characters.`;
        return `Use ${USERNAME_MAX} characters or fewer.`;
      case 'email':
        if (control.hasError('required')) return 'Enter your email address.';
        return 'Enter a valid email address.';
      case 'password':
        if (control.hasError('required')) return 'Choose a password.';
        return `Use at least ${PASSWORD_MIN} characters.`;
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    this.auth
      .register(this.form.getRawValue())
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Account created. Welcome to Book Tracker!');
          void this.router.navigateByUrl('/library');
        },
        error: (error: unknown) => this.serverError.set(errorMessage(error)),
      });
  }
}
