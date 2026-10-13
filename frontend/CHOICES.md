# Technical choices

Why the frontend is built the way it is. Each section states the decision, the reason, and what
was given up, so a future change can be made knowingly rather than by guesswork.

## Framework: Angular 20

The project brief asked for Angular. Version 20 is pinned because it is the release this code
was written and reviewed against: standalone components by default, stable signals
(`signal`, `computed`, `linkedSignal`, signal inputs), built-in control flow (`@if`, `@for`,
`@switch`) and the esbuild-based `@angular/build` builders.

Newer majors may exist by the time you read this. Upgrading is `ng update @angular/core
@angular/cli`; nothing here relies on APIs that later majors remove.

## Standalone components, no NgModules

Every component, guard and interceptor is standalone or functional. That is Angular's default
since v19, it removes a layer of indirection, and each file declares exactly what it imports.

## State: signals in services, no NgRx

State is small and has clear owners: `AuthService` owns the session, `LibraryService` owns the
user's books, `NotificationService` owns toasts. Each exposes read-only signals and a handful of
methods. Templates read signals directly and change detection is `OnPush` everywhere.

NgRx (actions, reducers, effects, selectors) would roughly triple the code for this scope with no
behavioural gain. If the app grows multiple features that edit the same data, NgRx SignalStore is
the natural next step and fits the same signal-based components.

RxJS is still used where it is the better tool: the debounced, cancellable search pipeline, and
HTTP itself.

## Forms: reactive forms

`NonNullableFormBuilder` gives typed controls with no `null` in their value types, validators are
declared next to the fields, and errors are easy to derive per field. Messages only appear after
a field is touched, or on submit, so users aren't shouted at while typing.

Client-side rules are slightly stricter than the backend's `@NotBlank` (username 3–40 characters,
valid email, password of 8+ characters). The backend stays the source of truth and any error it
returns is shown inline.

## Styling: Tailwind CSS v4, no component library

The brief ruled out a component library, so polish comes from Tailwind utilities plus a short set
of shared classes in `styles.css` (`.btn`, `.input`, `.card`, `.chip`, …). Brand colours are
defined once as `--color-brand-*` theme tokens; templates use `brand-*`, never a raw hue, so
re-theming is a single-file change.

Tailwind v4 needs no `tailwind.config.js`; it is wired in through `.postcssrc.json`, which the
Angular builder picks up automatically.

What we give up: accessible widgets like comboboxes, dialogs and menus come for free in Angular
Material or the CDK. This MVP needs none of them; native `<select>`, `<button>` and links cover
everything. If a dialog is ever needed, add `@angular/cdk` for focus trapping rather than
hand-rolling it.

## Icons: small in-house component instead of `lucide-angular`

The plan named `lucide-angular`. I swapped it for `shared/icon.ts`, which inlines the 15 Lucide
outlines the app uses (ISC-licensed path data). Two reasons:

- Every UI dependency carries an Angular peer-dependency range that has to keep pace with Angular
  majors. When it falls behind, `npm install` fails with `ERESOLVE` until the library catches up.
- The app uses 15 icons. A typed `IconName` union and one `@switch` is less code to own than a
  dependency.

Adding an icon means adding its name to the union and one `@case`.

## Animations: CSS only, no `@angular/animations`

`@angular/animations` is deprecated since Angular 20.2 in favour of native CSS. Toasts, cards and
empty states use two Tailwind keyframes (`animate-toast-in`, `animate-fade-in`). Every animation
is wrapped in `motion-safe:`, so users with `prefers-reduced-motion` get none.

## Authentication

**Token storage: `localStorage`.** It keeps the user signed in across reloads with no backend
changes. The known trade-off is that any XSS can read the token. Mitigations:

- No `innerHTML` and no `bypassSecurityTrust*` anywhere; Angular escapes every binding.
- The token is attached only to requests aimed at this app's API (`auth.interceptor.ts`), never
  to other origins.

The stronger option is an `HttpOnly`, `Secure`, `SameSite` cookie set by the backend. That needs
backend work (a cookie on login, CSRF protection for state-changing requests), so it is out of
MVP scope but is the recommended next step before handling sensitive data.

**Expiry is enforced client-side.** The login response includes `expiresIn`; the service stores
the absolute expiry and signs the user out the moment it passes, instead of waiting for the next
request to fail. A 401 on any authenticated call also signs out and returns to login with a
`returnUrl`.

**`returnUrl` is validated.** Only same-app paths are accepted (`return-url.ts`), so a crafted
link like `/login?returnUrl=//evil.example` can't redirect a user off-site after they sign in.

**The username comes from the JWT `sub` claim**, decoded for display only. The frontend never
trusts the token's contents for authorisation; the backend checks every request.

**Register signs the user straight in**, so a new account lands in its library instead of on the
login form.

## HTTP and error handling

Two functional interceptors, in order:

1. `authInterceptor` adds `Authorization: Bearer …` to API calls.
2. `errorInterceptor` handles 401s (sign out) and turns other failures into toasts using the
   backend's `ErrorResponseDTO.message`, falling back to a status-based message when the body is
   empty or not JSON.

Some screens show errors inline: the login and register forms, the library's first load, and
search. They mark requests with `silentErrors()` (an `HttpContextToken`) so the user isn't told
twice. Errors are always rethrown, so callers can still roll back.

## Library data

**Load once, filter on the client.** The backend supports `?status=`, but a personal library is
small. Loading it once makes filter switches instant and the per-status counts on the chips free.

**Filter state lives in the URL** (`/library?status=READING`), bound to a component input with
`withComponentInputBinding()`. It survives reloads, works with the back button, and can be
bookmarked. Search does the same with `?q=`.

**Optimistic updates.** Progress and status changes appear immediately, are replaced by the
server's response (the backend may also change dates, or move a book to `READING`), and roll back
if the request fails. The update is wrapped in `defer`, so nothing changes locally unless the
request actually runs.

**The cache is scoped to the session.** `LibraryService` remembers which token loaded the list.
If a different user signs in, the old list is cleared before anything renders, and a late
response from the previous session is discarded.

## Search

The input is debounced (350 ms), deduplicated, and piped through `switchMap`, so a slow response
can never overwrite results for a newer query. Queries shorter than 3 characters aren't sent.
Results that are already in the library show "In your library" instead of an add button.

## Dates

The backend sends `LocalDate` values as `yyyy-MM-dd`. `new Date('2026-01-15')` parses that as UTC
midnight, which displays as the previous day anywhere west of UTC. `shared/format.ts` builds the
date from its parts in local time.

## Accessibility

- A skip-to-content control, a `<main>` landmark and labelled `<nav>` regions.
- Every input has a `<label>` (visually hidden where the design calls for it), plus
  `aria-invalid` and `aria-describedby` pointing at its error.
- `focus-visible` rings on all interactive elements.
- Toasts and the search status line are polite live regions; standalone spinners announce
  themselves.
- Progress bars expose `role="progressbar"` with current values.
- Icons are `aria-hidden`; icon-only buttons carry an `aria-label`.

## Connecting to the backend

In development the app calls `http://localhost:8080/api/v1` directly. The backend already allows
`http://localhost:4200` via `CORS_ALLOWED_ORIGINS`, so no dev-server proxy is needed.

The production build uses a relative `/api/v1` and expects to be served from the same origin as
the API (behind a reverse proxy). That avoids CORS in production altogether. If you deploy them
on different origins, set `apiBaseUrl` in `environment.ts` and add the frontend origin to the
backend's `CORS_ALLOWED_ORIGINS`.

## Dependencies are pinned exactly

`package.json` uses exact versions, not `^` ranges, so every install resolves the same tree and an
upstream release can't change behaviour silently. Upgrades are deliberate: bump the versions,
reinstall, and run the tests.

## Testing: Jasmine and Karma

This is what Angular 20's `ng test` builder (`@angular/build:karma`) is set up for, so it works
with no extra configuration. Specs cover the logic most likely to regress quietly: open-redirect
protection, error-message selection, token attachment, and the optimistic-update/rollback and
caching rules in `LibraryService`.

Vitest support in the Angular CLI was still experimental in v20; switching later is a builder
change. End-to-end tests (Playwright) are deferred.

## Deliberately out of scope

- Reading-statistics page (`/books/me/stats` is ready on the backend).
- Removing a book from the library: the backend has no delete endpoint yet.
- Cookie-based auth and refresh tokens.
- Dark mode. The theme tokens make it a contained change.
- ESLint/Prettier (`ng add @angular-eslint/schematics` when wanted).
- SSR and i18n.

## Known backend gap

`POST /books/me` accepts no cover URL, so tracked books are stored without one and the library
always shows the generated placeholder cover, even though search results have real covers.
Adding an optional `coverImgUrl` to `TrackBookRequestDTO` on the backend and sending it from
`LibraryService.track()` would fix it.
