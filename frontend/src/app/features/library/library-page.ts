import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  READING_STATUS_LABELS,
  READING_STATUSES,
  ReadingStatus,
  isReadingStatus,
} from '../../core/models/reading-status';
import { BookCardSkeleton } from '../../shared/book-card-skeleton';
import { EmptyState } from '../../shared/empty-state';
import { Icon } from '../../shared/icon';
import { Spinner } from '../../shared/spinner';
import { BookCard } from './book-card';
import { LibraryService } from './library.service';

type Filter = ReadingStatus | 'ALL';

interface FilterOption {
  value: Filter;
  /** Query parameter value; null removes it, which is how "All" is expressed in the URL. */
  param: ReadingStatus | null;
  label: string;
  count: number;
}

@Component({
  selector: 'app-library-page',
  imports: [RouterLink, BookCard, BookCardSkeleton, EmptyState, Icon, Spinner],
  templateUrl: './library-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LibraryPage {
  protected readonly library = inject(LibraryService);

  /**
   * Bound from `?status=`. Keeping the filter in the URL means it survives a reload, works
   * with the back button, and can be bookmarked.
   */
  readonly status = input<string>();

  protected readonly skeletons = [1, 2, 3, 4, 5, 6];

  protected readonly filter = computed<Filter>(() => {
    const status = this.status();
    return isReadingStatus(status) ? status : 'ALL';
  });

  protected readonly filterOptions = computed<FilterOption[]>(() => {
    const counts = this.library.counts();
    return [
      { value: 'ALL', param: null, label: 'All', count: this.library.books().length },
      ...READING_STATUSES.map((status) => ({
        value: status,
        param: status,
        label: READING_STATUS_LABELS[status],
        count: counts[status],
      })),
    ];
  });

  /** Most recently tracked first; ids are assigned in insertion order by the database. */
  protected readonly visibleBooks = computed(() => {
    const filter = this.filter();
    return this.library
      .books()
      .filter((book) => filter === 'ALL' || book.status === filter)
      .sort((a, b) => b.id - a.id);
  });

  protected readonly filterLabel = computed(() => {
    const filter = this.filter();
    return filter === 'ALL' ? '' : READING_STATUS_LABELS[filter].toLowerCase();
  });

  protected readonly summary = computed(() => {
    const total = this.library.books().length;
    const reading = this.library.counts().READING;
    const books = `${total} ${total === 1 ? 'book' : 'books'}`;
    return reading > 0 ? `${books} · ${reading} in progress` : books;
  });

  constructor() {
    this.library.ensureLoaded();
  }
}
