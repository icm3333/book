/**
 * Development settings, swapped in by `ng serve` via the fileReplacements in angular.json.
 *
 * The backend allows http://localhost:4200 through CORS_ALLOWED_ORIGINS, so the dev server
 * calls it directly.
 */
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1',
};
