import { ReadingStatus } from './reading-status';

/** Mirrors `UserBookResponseDTO`. Dates are ISO `yyyy-MM-dd` strings (Java `LocalDate`). */
export interface UserBook {
  id: number;
  title: string;
  author: string | null;
  openLibraryId: string;
  coverImgUrl: string | null;
  pageCount: number | null;
  status: ReadingStatus;
  currentPage: number | null;
  startDate: string | null;
  finishDate: string | null;
}

/** Mirrors `TrackBookRequestDTO`. */
export interface TrackBookRequest {
  openLibraryId: string;
  title: string;
  author: string | null;
  pageCount: number | null;
}
