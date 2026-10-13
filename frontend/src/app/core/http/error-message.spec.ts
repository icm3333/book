import { HttpErrorResponse } from '@angular/common/http';

import { errorMessage } from './error-message';

describe('errorMessage', () => {
  it('uses the message from the backend error body', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { status: 409, error: 'Conflict', message: 'Email already in use.', path: '/x', timestamp: '' },
    });
    expect(errorMessage(error)).toBe('Email already in use.');
  });

  it('falls back to a status message when the body is empty', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 401 }))).toContain('session has expired');
  });

  it('explains a network failure instead of showing a blank message', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('Cannot reach the server');
  });

  it('treats upstream outages as a catalogue problem', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 502, error: '<html>Bad gateway</html>' }))).toContain(
      'catalogue is unavailable',
    );
  });

  it('handles non-HTTP errors', () => {
    expect(errorMessage(new Error('boom'))).toBe('Something went wrong. Please try again.');
  });
});
