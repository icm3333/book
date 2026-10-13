import { HttpErrorResponse } from '@angular/common/http';

import { isApiErrorResponse } from '../models/error-response';

/**
 * Picks the message to show the user for a failed request.
 *
 * Prefers the backend's own `message`, which is already written for humans. Falls back to a
 * status-based message when the body is missing, for example the empty 401 from the security
 * entry point, a network failure, or an HTML error page from a proxy.
 */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const body: unknown = error.error;
    if (error.status !== 0 && isApiErrorResponse(body) && body.message.trim() !== '') {
      return body.message;
    }
    return fallbackFor(error.status);
  }
  return fallbackFor(-1);
}

function fallbackFor(status: number): string {
  switch (status) {
    case 0:
      return 'Cannot reach the server. Check your connection and that the API is running.';
    case 401:
      return 'Your session has expired. Please sign in again.';
    case 403:
      return 'You do not have permission to do that.';
    case 404:
      return 'We could not find what you were looking for.';
    case 502:
    case 503:
    case 504:
      return 'The book catalogue is unavailable right now. Please try again shortly.';
    default:
      return 'Something went wrong. Please try again.';
  }
}
