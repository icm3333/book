const DEFAULT_URL = '/library';

/**
 * Validates a post-login redirect target taken from the query string.
 *
 * Only same-app absolute paths are accepted. `//evil.example` and `/\evil.example` are
 * protocol-relative URLs that browsers resolve to another origin, so they are rejected, as is
 * anything pointing back at the auth pages.
 */
export function safeReturnUrl(url: string | null | undefined): string {
  if (
    !url ||
    !url.startsWith('/') ||
    url.startsWith('//') ||
    url.startsWith('/\\') ||
    url.startsWith('/login') ||
    url.startsWith('/register')
  ) {
    return DEFAULT_URL;
  }
  return url;
}
