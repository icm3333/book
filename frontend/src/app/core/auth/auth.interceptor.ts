import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

/**
 * Adds `Authorization: Bearer <token>` to requests aimed at this app's API.
 *
 * Requests to any other origin never receive the token, so a third-party URL can't capture it.
 * The public auth endpoints are skipped as well.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).token();
  const isApiCall = request.url.startsWith(environment.apiBaseUrl);
  const isAuthCall = request.url.startsWith(`${environment.apiBaseUrl}/auth/`);

  if (!token || !isApiCall || isAuthCall) {
    return next(request);
  }

  return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
