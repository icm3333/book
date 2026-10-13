import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { NotificationService } from '../notifications/notification.service';
import { errorMessage } from './error-message';
import { SILENT_ERRORS } from './http-context';

/**
 * Central place for request failures.
 *
 * - A 401 on an authenticated call means the token was rejected: sign out and return to login.
 *   401s from the auth endpoints are wrong credentials, which the form reports itself.
 * - Anything else raises a toast, unless the request was marked with `silentErrors()`.
 *
 * The error is always rethrown so callers can still roll back or update their own state.
 */
export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const notifications = inject(NotificationService);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        const isAuthCall = request.url.startsWith(`${environment.apiBaseUrl}/auth/`);

        if (error.status === 401 && !isAuthCall) {
          // Several requests can fail together when a token lapses; only the first signs out,
          // and none of them add a toast on top of the sign-out notice.
          if (auth.isAuthenticated()) {
            auth.logout('expired');
          }
        } else if (!request.context.get(SILENT_ERRORS)) {
          notifications.error(errorMessage(error));
        }
      }
      return throwError(() => error);
    }),
  );
};
