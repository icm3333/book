import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { UserBook } from '../../core/models/user-book';
import { LibraryService } from './library.service';

function book(overrides: Partial<UserBook> = {}): UserBook {
  return {
    id: 1,
    title: 'Structure and Interpretation of Computer Programs',
    author: 'Harold Abelson',
    openLibraryId: '/works/OL1W',
    coverImgUrl: null,
    pageCount: 657,
    status: 'TO_READ',
    currentPage: 0,
    startDate: null,
    finishDate: null,
    ...overrides,
  };
}

describe('LibraryService', () => {
  const baseUrl = `${environment.apiBaseUrl}/books/me`;
  let service: LibraryService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(LibraryService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function loadWith(books: UserBook[]): void {
    service.reload();
    http.expectOne(baseUrl).flush(books);
  }

  it('counts books per status', () => {
    loadWith([book({ id: 1 }), book({ id: 2, status: 'READING' }), book({ id: 3, status: 'READING' })]);

    expect(service.counts()).toEqual({ TO_READ: 1, READING: 2, COMPLETED: 0 });
    expect(service.loaded()).toBeTrue();
  });

  it('applies a progress update immediately and then adopts the server version', () => {
    loadWith([book({ currentPage: 10 })]);

    service.updateProgress(1, 50).subscribe();
    expect(service.books()[0].currentPage).toBe(50);

    const request = http.expectOne(`${baseUrl}/1/progress`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ currentPage: 50 });

    // The backend moves TO_READ books to READING when progress is recorded.
    request.flush(book({ currentPage: 50, status: 'READING', startDate: '2026-10-13' }));
    expect(service.books()[0].status).toBe('READING');
  });

  it('restores the previous value when the update fails', () => {
    loadWith([book({ currentPage: 10 })]);

    service.updateStatus(1, 'COMPLETED').subscribe({ error: () => undefined });
    expect(service.books()[0].status).toBe('COMPLETED');

    http
      .expectOne(`${baseUrl}/1/status`)
      .flush({ message: 'Tracking record not found' }, { status: 404, statusText: 'Not Found' });
    expect(service.books()[0].status).toBe('TO_READ');
  });

  it('does not touch local state until the update is subscribed to', () => {
    loadWith([book({ currentPage: 10 })]);

    service.updateProgress(1, 99);

    expect(service.books()[0].currentPage).toBe(10);
    http.expectNone(`${baseUrl}/1/progress`);
  });

  it('adds a newly tracked book to the front of the list', () => {
    loadWith([book({ id: 1 })]);

    service
      .track({ openLibraryId: '/works/OL2W', title: 'Dune', author: 'Frank Herbert', coverImgUrl: null, pageCount: 412 })
      .subscribe();

    const request = http.expectOne(baseUrl);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      openLibraryId: '/works/OL2W',
      title: 'Dune',
      author: 'Frank Herbert',
      pageCount: 412,
    });
    request.flush(book({ id: 2, openLibraryId: '/works/OL2W', title: 'Dune' }));

    expect(service.books().map((b) => b.id)).toEqual([2, 1]);
    expect(service.trackedIds().has('/works/OL2W')).toBeTrue();
  });

  it('reports a failed load without discarding the previous list', () => {
    loadWith([book()]);

    service.reload();
    http.expectOne(baseUrl).flush(null, { status: 500, statusText: 'Server Error' });

    expect(service.failed()).toBeTrue();
    expect(service.books().length).toBe(1);
  });
});
