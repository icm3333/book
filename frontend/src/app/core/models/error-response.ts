/** Mirrors `ErrorResponseDTO`, the single shape every failing backend endpoint returns. */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as Partial<ApiErrorResponse>).message === 'string'
  );
}
