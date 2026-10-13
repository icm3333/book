import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { silentErrors } from '../../core/http/http-context';
import { BookSearchResult } from '../../core/models/book-search-result';

/**
 * Searches Open Library through the backend. Debouncing is the page's job, because it depends
 * on how the input is used rather than on the API.
 */
@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly http = inject(HttpClient);

  search(query: string): Observable<BookSearchResult[]> {
    return this.http.get<BookSearchResult[]>(`${environment.apiBaseUrl}/books/search`, {
      params: { query },
      // The page shows search failures inline instead of as a toast.
      context: silentErrors(),
    });
  }
}
