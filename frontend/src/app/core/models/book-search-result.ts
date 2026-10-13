/** Mirrors `BookSearchResultDTO`. The backend substitutes "Unknown" when no author is listed. */
export interface BookSearchResult {
  openLibraryId: string;
  title: string;
  author: string;
  coverImgUrl: string | null;
  pageCount: number | null;
}
