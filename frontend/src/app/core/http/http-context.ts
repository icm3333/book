import { HttpContext, HttpContextToken } from '@angular/common/http';

/**
 * Marks a request whose errors are shown inline by the caller, so the global error interceptor
 * does not also raise a toast for them. 401 session handling still applies.
 */
export const SILENT_ERRORS = new HttpContextToken<boolean>(() => false);

export function silentErrors(): HttpContext {
  return new HttpContext().set(SILENT_ERRORS, true);
}
