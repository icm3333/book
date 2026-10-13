/** Mirrors the backend `ReadingStatus` enum. */
export type ReadingStatus = 'TO_READ' | 'READING' | 'COMPLETED';

export const READING_STATUSES: readonly ReadingStatus[] = ['TO_READ', 'READING', 'COMPLETED'];

export const READING_STATUS_LABELS: Record<ReadingStatus, string> = {
  TO_READ: 'To read',
  READING: 'Reading',
  COMPLETED: 'Completed',
};

/** Tailwind classes for the status pill. Full class names so Tailwind's scanner picks them up. */
export const READING_STATUS_BADGE: Record<ReadingStatus, string> = {
  TO_READ: 'bg-slate-100 text-slate-700 ring-slate-500/20',
  READING: 'bg-amber-50 text-amber-800 ring-amber-600/20',
  COMPLETED: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
};

export function isReadingStatus(value: unknown): value is ReadingStatus {
  return typeof value === 'string' && (READING_STATUSES as readonly string[]).includes(value);
}
