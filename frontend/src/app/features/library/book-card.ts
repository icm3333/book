import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  input,
  linkedSignal,
  signal,
} from '@angular/core';
import { finalize } from 'rxjs';

import { NotificationService } from '../../core/notifications/notification.service';
import {
  READING_STATUS_BADGE,
  READING_STATUS_LABELS,
  READING_STATUSES,
  isReadingStatus,
} from '../../core/models/reading-status';
import { UserBook } from '../../core/models/user-book';
import { BookCover } from '../../shared/book-cover';
import { formatIsoDate } from '../../shared/format';
import { Icon } from '../../shared/icon';
import { Spinner } from '../../shared/spinner';
import { LibraryService } from './library.service';

/** How long the "Saved" confirmation stays visible after a progress update. */
const SAVED_FLASH_MS = 2000;

@Component({
  selector: 'app-book-card',
  imports: [BookCover, Icon, Spinner],
  templateUrl: './book-card.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block h-full' },
})
export class BookCard {
  private readonly library = inject(LibraryService);
  private readonly notifications = inject(NotificationService);

  readonly book = input.required<UserBook>();

  protected readonly statuses = READING_STATUSES;
  protected readonly labels = READING_STATUS_LABELS;

  /** Editable copy of the current page; resets whenever the book changes underneath it. */
  protected readonly pageDraft = linkedSignal(() => this.book().currentPage ?? 0);
  protected readonly savingProgress = signal(false);
  protected readonly savingStatus = signal(false);
  protected readonly justSaved = signal(false);
  private savedTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly currentPage = computed(() => this.book().currentPage ?? 0);
  protected readonly dirty = computed(() => this.pageDraft() !== this.currentPage());

  protected readonly atMax = computed(() => {
    const total = this.book().pageCount;
    return !!total && this.pageDraft() >= total;
  });

  protected readonly percent = computed(() => {
    const total = this.book().pageCount;
    return total ? Math.min(100, Math.round((this.currentPage() / total) * 100)) : null;
  });

  protected readonly pageError = computed(() => {
    const page = this.pageDraft();
    const total = this.book().pageCount;
    if (!Number.isInteger(page)) return 'Enter a whole page number.';
    if (page < 0) return 'Page cannot be negative.';
    if (total && page > total) return `This book has ${total} pages.`;
    return null;
  });

  protected readonly showPageError = computed(() => this.dirty() && this.pageError() !== null);
  protected readonly started = computed(() => formatIsoDate(this.book().startDate));
  protected readonly finished = computed(() => formatIsoDate(this.book().finishDate));
  protected readonly badgeClass = computed(() => READING_STATUS_BADGE[this.book().status]);

  protected readonly ids = computed(() => {
    const id = this.book().id;
    return {
      title: `book-${id}-title`,
      page: `book-${id}-page`,
      pageError: `book-${id}-page-error`,
      status: `book-${id}-status`,
    };
  });

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.savedTimer));
  }

  protected onPageInput(event: Event): void {
    // An empty or partial entry becomes NaN, which pageError reports.
    this.pageDraft.set((event.target as HTMLInputElement).valueAsNumber);
  }

  protected step(delta: number): void {
    const base = Number.isInteger(this.pageDraft()) ? this.pageDraft() : this.currentPage();
    const max = this.book().pageCount || Number.POSITIVE_INFINITY;
    this.pageDraft.set(Math.min(max, Math.max(0, base + delta)));
  }

  protected resetDraft(): void {
    this.pageDraft.set(this.currentPage());
  }

  protected saveProgress(): void {
    if (!this.dirty() || this.pageError() !== null || this.savingProgress()) {
      return;
    }

    this.savingProgress.set(true);
    this.library
      .updateProgress(this.book().id, this.pageDraft())
      .pipe(finalize(() => this.savingProgress.set(false)))
      .subscribe({
        next: () => this.flashSaved(),
        // The error interceptor has already shown a toast and the service rolled back.
        error: () => this.resetDraft(),
      });
  }

  protected changeStatus(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if (!isReadingStatus(value) || value === this.book().status) {
      return;
    }

    const title = this.book().title;
    this.savingStatus.set(true);
    this.library
      .updateStatus(this.book().id, value)
      .pipe(finalize(() => this.savingStatus.set(false)))
      .subscribe({
        next: (updated) => {
          if (updated.status === 'COMPLETED') {
            this.notifications.success(`Finished “${title}”. Nice work!`);
          }
        },
        // Already reported by the error interceptor; the service restored the old status.
        error: () => undefined,
      });
  }

  private flashSaved(): void {
    clearTimeout(this.savedTimer);
    this.justSaved.set(true);
    this.savedTimer = setTimeout(() => this.justSaved.set(false), SAVED_FLASH_MS);
  }
}
