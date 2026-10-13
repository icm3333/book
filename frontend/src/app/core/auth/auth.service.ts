import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, switchMap, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { silentErrors } from '../http/http-context';
import { LoginRequest, RegisterRequest, TokenResponse } from '../models/auth';
import { NotificationService } from '../notifications/notification.service';
import { StoredSession, TokenStorage } from './token-storage';

/** Largest delay setTimeout accepts; longer values overflow and fire immediately. */
const MAX_TIMEOUT_MS = 2_147_483_647;

/**
 * Owns the signed-in session.
 *
 * State is a single signal; everything else (token, username, isAuthenticated) is derived, so
 * the interceptor, guards and header can never disagree about whether the user is signed in.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly storage = inject(TokenStorage);
  private readonly notifications = inject(NotificationService);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly session = signal<StoredSession | null>(this.storage.read());
  private expiryTimer: ReturnType<typeof setTimeout> | undefined;

  readonly token = computed(() => this.session()?.token ?? null);
  readonly isAuthenticated = computed(() => this.session() !== null);
  readonly username = computed(() => {
    const token = this.token();
    return token ? usernameFromToken(token) : null;
  });

  constructor() {
    const existing = this.session();
    if (existing) {
      this.scheduleExpiry(existing.expiresAt);
    }
  }

  /** Errors are left to the caller: the auth forms show them inline. */
  login(credentials: LoginRequest): Observable<void> {
    return this.http
      .post<TokenResponse>(`${this.baseUrl}/login`, credentials, { context: silentErrors() })
      .pipe(
        tap((response) => this.startSession(response)),
        map(() => undefined),
      );
  }

  /** Registers, then signs straight in so a new user lands in their library. */
  register(request: RegisterRequest): Observable<void> {
    return this.http
      .post<void>(`${this.baseUrl}/register`, request, { context: silentErrors() })
      .pipe(switchMap(() => this.login({ username: request.username, password: request.password })));
  }

  logout(reason: 'user' | 'expired' = 'user'): void {
    const returnUrl = this.router.url;
    this.endSession();

    if (reason === 'expired') {
      this.notifications.info('Your session expired. Please sign in again.');
      void this.router.navigate(['/login'], { queryParams: { returnUrl } });
    } else {
      this.notifications.info('You have been signed out.');
      void this.router.navigate(['/login']);
    }
  }

  private startSession(response: TokenResponse): void {
    const session: StoredSession = {
      token: response.token,
      expiresAt: Date.now() + response.expiresIn,
    };
    this.storage.write(session);
    this.session.set(session);
    this.scheduleExpiry(session.expiresAt);
  }

  private endSession(): void {
    clearTimeout(this.expiryTimer);
    this.expiryTimer = undefined;
    this.storage.clear();
    this.session.set(null);
  }

  /**
   * Signs out the moment the token lapses instead of waiting for the next request to fail.
   * Very long lifetimes skip the timer; the 401 handling in the error interceptor covers them.
   */
  private scheduleExpiry(expiresAt: number): void {
    clearTimeout(this.expiryTimer);
    const delay = expiresAt - Date.now();
    if (delay <= MAX_TIMEOUT_MS) {
      this.expiryTimer = setTimeout(() => this.logout('expired'), Math.max(0, delay));
    }
  }
}

/**
 * Reads the `sub` claim, which the backend sets to the username, for display purposes only.
 * The signature is not checked here; the backend verifies every request.
 */
function usernameFromToken(token: string): string | null {
  const payload = token.split('.')[1];
  if (!payload) {
    return null;
  }
  try {
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    const claims: unknown = JSON.parse(atob(padded));
    const subject = (claims as { sub?: unknown } | null)?.sub;
    return typeof subject === 'string' ? subject : null;
  } catch {
    return null;
  }
}
