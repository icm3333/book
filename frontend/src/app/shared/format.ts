const DATE_FORMAT = new Intl.DateTimeFormat(undefined, {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
});

/**
 * Formats a `yyyy-MM-dd` date from the backend in the user's locale.
 *
 * `new Date('2026-01-15')` is parsed as UTC midnight, which displays as the previous day in any
 * timezone west of UTC (Brazil included). Building the date from its parts keeps it local.
 */
export function formatIsoDate(iso: string | null): string | null {
  if (!iso) {
    return null;
  }
  const [year, month, day] = iso.split('-').map(Number);
  if (!year || !month || !day) {
    return null;
  }
  return DATE_FORMAT.format(new Date(year, month - 1, day));
}
