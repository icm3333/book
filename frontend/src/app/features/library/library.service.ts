import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, defer, tap, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/auth/auth.service';
import { silentErrors } from '../../core/http/http-context';
import { BookSearchResult } from '../../core/models/book-search-result';
import { ReadingStatus } from '../../core/models/reading-status';
import { TrackBookRequest, UserBook } from '../../core/models/user-book';

type LoadStatus = 'idle' | 'loading' | 'loaded' | 'error';

/**
 * The signed-in user's library, shared by the library and search pages.
 *
 * The whole list is loaded once and filtered client-side: a personal library is small, filter
 * switches become instant, and the per-status counts come for free.
 *
 * The cache is tied to the session token that loaded it. Signing out and in as someone else
 * therefore never shows the previous user's books, even for a moment, and a response that
 * arrives after the session changed is discarded.
 */
@Injectable({ providedIn: 'root' })
export class LibraryService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = `${environment.apiBaseUrl}/books/me`;

  private owner: string | null = null;
  private readonly _books = signal<readonly UserBook[]>([]);
  private readonly _status = signal<LoadStatus>('idle');

  readonly books = this._books.asReadonly();
  readonly loading = computed(() => this._status() === 'loading');
  readonly loaded = computed(() => this._status() === 'loaded');
  readonly failed = computed(() => this._status() === 'error');

  /** Open Library ids already tracked, so search results can show "In your library". */
  readonly trackedIds = computed(() => new Set(this._books().map((book) => book.openLibraryId)));

  readonly counts = computed(() => {
    const counts: Record<ReadingStatus, number> = { TO_READ: 0, READING: 0, COMPLETED: 0 };
    for (const book of this._books()) {
      counts[book.status]++;
    }
    return counts;
  });

  /** Loads the library unless it is already loaded, or loading, for the current session. */
  ensureLoaded(): void {
    const status = this._status();
    if (this.owner !== this.auth.token() || status === 'idle' || status === 'error') {
      this.reload();
    }
  }

  reload(): void {
    const owner = this.auth.token();
    if (owner !== this.owner) {
      this.owner = owner;
      this._books.set([]);
    }

    this._status.set('loading');
    // Load failures are shown inline by the page, so no toast.
    this.http.get<UserBook[]>(this.baseUrl, { context: silentErrors() }).subscribe({
      next: (books) => {
        if (this.owner === owner) {
          this._books.set(books);
          this._status.set('loaded');
        }
      },
      error: () => {
        if (this.owner === owner) {
          this._status.set('error');
        }
      },
    });
  }

  track(book: BookSearchResult): Observable<UserBook> {
    const body: TrackBookRequest = {
      openLibraryId: book.openLibraryId,
      title: book.title,
      author: book.author,
      pageCount: book.pageCount,
    };
    return this.http
      .post<UserBook>(this.baseUrl, body)
      .pipe(tap((created) => this._books.update((books) => [created, ...books])));
  }

  updateProgress(id: number, currentPage: number): Observable<UserBook> {
    return this.mutate(
      id,
      (book) => ({ ...book, currentPage }),
      () => this.http.patch<UserBook>(`${this.baseUrl}/${id}/progress`, { currentPage }),
    );
  }

  updateStatus(id: number, status: ReadingStatus): Observable<UserBook> {
    return this.mutate(
      id,
      (book) => ({ ...book, status }),
      () => this.http.patch<UserBook>(`${this.baseUrl}/${id}/status`, { status }),
    );
  }

  /**
   * Applies `optimistic` locally as soon as the caller subscribes, replaces it with the server's
   * version on success (the backend may change other fields, such as dates or the status),
   * and restores the previous value if the request fails.
   */
  private mutate(
    id: number,
    optimistic: (book: UserBook) => UserBook,
    request: () => Observable<UserBook>,
  ): Observable<UserBook> {
    return defer(() => {
      const previous = this._books().find((book) => book.id === id);
      if (previous) {
        this.replace(optimistic(previous));
      }
      return request().pipe(
        tap((updated) => this.replace(updated)),
        catchError((error: unknown) => {
          if (previous) {
            this.replace(previous);
          }
          return throwError(() => error);
        }),
      );
    });
  }

  private replace(updated: UserBook): void {
    this._books.update((books) => books.map((book) => (book.id === updated.id ? updated : book)));
  }
}
