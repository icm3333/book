/**
 * Production settings.
 *
 * The API is expected on the same origin behind a reverse proxy, so the base URL is relative
 * and no CORS configuration is needed in production.
 */
export const environment = {
  production: true,
  apiBaseUrl: '/api/v1',
};
