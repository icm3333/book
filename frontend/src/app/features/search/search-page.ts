import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnInit,
  afterNextRender,
  computed,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  Observable,
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
  merge,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { errorMessage } from '../../core/http/error-message';
import { BookSearchResult } from '../../core/models/book-search-result';
import { NotificationService } from '../../core/notifications/notification.service';
import { BookCardSkeleton } from '../../shared/book-card-skeleton';
import { BookCover } from '../../shared/book-cover';
import { EmptyState } from '../../shared/empty-state';
import { Icon } from '../../shared/icon';
import { Spinner } from '../../shared/spinner';
import { LibraryService } from '../library/library.service';
import { SearchService } from './search.service';

/** Shorter queries return noisy results and waste upstream calls. */
export const MIN_QUERY_LENGTH = 3;
const DEBOUNCE_MS = 350;

type SearchState =
  | { kind: 'idle' }
  | { kind: 'loading'; query: string }
  | { kind: 'results'; query: string; results: readonly BookSearchResult[] }
  | { kind: 'error'; message: string };

@Component({
  selector: 'app-search-page',
  imports: [ReactiveFormsModule, RouterLink, BookCardSkeleton, BookCover, EmptyState, Icon, Spinner],
  templateUrl: './search-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchPage implements OnInit {
  private readonly searchService = inject(SearchService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  protected readonly library = inject(LibraryService);

  /** Bound from `?q=`, so a search survives a reload and the back button. */
  readonly q = input<string>();

  protected readonly minLength = MIN_QUERY_LENGTH;
  protected readonly skeletons = [1, 2, 3, 4, 5, 6];
  protected readonly query = new FormControl('', { nonNullable: true });
  protected readonly state = signal<SearchState>({ kind: 'idle' });
  /** Open Library ids with an add request in flight. */
  protected readonly adding = signal<ReadonlySet<string>>(new Set());

  private readonly retry$ = new Subject<void>();
  private readonly searchInput = viewChild.required<ElementRef<HTMLInputElement>>('searchInput');

  protected readonly results = computed(() => {
    const state = this.state();
    return state.kind === 'results' ? state.results : [];
  });

  protected readonly failure = computed(() => {
    const state = this.state();
    return state.kind === 'error' ? state.message : null;
  });

  /** Read out by screen readers via the polite live region under the input. */
  protected readonly statusText = computed(() => {
    const state = this.state();
    switch (state.kind) {
      case 'idle':
        return `Type at least ${MIN_QUERY_LENGTH} characters to search.`;
      case 'loading':
        return `Searching for “${state.query}”…`;
      case 'results': {
        const count = state.results.length;
        return `${count} ${count === 1 ? 'result' : 'results'} for “${state.query}”.`;
      }
      case 'error':
        return 'Search failed.';
    }
  });

  constructor() {
    this.library.ensureLoaded();

    const typed$ = this.query.valueChanges.pipe(
      map((value) => value.trim()),
      debounceTime(DEBOUNCE_MS),
      distinctUntilChanged(),
    );
    const retried$ = this.retry$.pipe(map(() => this.query.value.trim()));

    merge(typed$, retried$)
      .pipe(
        tap((query) => this.syncUrl(query)),
        // switchMap cancels the previous request, so slow responses can't overwrite newer ones.
        switchMap((query) => this.runSearch(query)),
        takeUntilDestroyed(),
      )
      .subscribe((state) => this.state.set(state));

    afterNextRender(() => this.searchInput().nativeElement.focus());
  }

  ngOnInit(): void {
    const initial = this.q()?.trim();
    if (initial) {
      this.query.setValue(initial);
    }
  }

  protected clear(): void {
    this.query.setValue('');
    this.searchInput().nativeElement.focus();
  }

  protected retry(): void {
    this.retry$.next();
  }

  protected isAdding(book: BookSearchResult): boolean {
    return this.adding().has(book.openLibraryId);
  }

  protected add(book: BookSearchResult): void {
    const id = book.openLibraryId;
    this.adding.update((ids) => new Set(ids).add(id));

    this.library
      .track(book)
      .pipe(
        finalize(() =>
          this.adding.update((ids) => {
            const next = new Set(ids);
            next.delete(id);
            return next;
          }),
        ),
      )
      .subscribe({
        next: () => this.notifications.success(`Added “${book.title}” to your library.`),
        // Already reported by the error interceptor (for example 409 when already tracked).
        error: () => undefined,
      });
  }

  private runSearch(query: string): Observable<SearchState> {
    if (query.length < MIN_QUERY_LENGTH) {
      return of<SearchState>({ kind: 'idle' });
    }
    return this.searchService.search(query).pipe(
      map((results): SearchState => ({ kind: 'results', query, results })),
      catchError((error: unknown) => of<SearchState>({ kind: 'error', message: errorMessage(error) })),
      startWith<SearchState>({ kind: 'loading', query }),
    );
  }

  private syncUrl(query: string): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { q: query || null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }
}
