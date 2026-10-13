import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { authInterceptor } from './auth.interceptor';
import { TokenStorage } from './token-storage';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    // Write the session before AuthService is created, so it starts signed in.
    TestBed.inject(TokenStorage).write({ token: 'test-token', expiresAt: Date.now() + 60_000 });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
    localStorage.clear();
  });

  it('attaches the bearer token to API requests', () => {
    http.get(`${environment.apiBaseUrl}/books/me`).subscribe();

    const request = controller.expectOne(`${environment.apiBaseUrl}/books/me`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer test-token');
    request.flush([]);
  });

  it('leaves the public auth endpoints alone', () => {
    http.post(`${environment.apiBaseUrl}/auth/login`, {}).subscribe();

    const request = controller.expectOne(`${environment.apiBaseUrl}/auth/login`);
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush({});
  });

  it('never sends the token to another origin', () => {
    http.get('https://covers.openlibrary.org/b/id/1-M.json').subscribe();

    const request = controller.expectOne('https://covers.openlibrary.org/b/id/1-M.json');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush({});
  });
});
