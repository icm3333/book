# Book Tracker — Frontend

Angular client for the [Book Tracker API](../README.md). Sign in, search Open Library, build a
reading list, and record progress.

Built with Angular 20 (standalone components and signals) and Tailwind CSS v4. Why each choice
was made is in [CHOICES.md](CHOICES.md).

## Requirements

- Node.js 20.19+ or 22.12+ (Angular 20's minimum)
- The backend running on `http://localhost:8080` (see the root README)

## Getting started

```bash
cd frontend
npm install
npm start
```

Open http://localhost:4200. Register an account and you land in your (empty) library; use
**Add books** to search and track titles.

The backend must allow the dev origin. It does by default (`CORS_ALLOWED_ORIGINS` defaults to
`http://localhost:4200`); if you serve the frontend on another port, update that variable.

## Scripts

| Command | Purpose |
| --- | --- |
| `npm start` | Dev server with live reload on port 4200 |
| `npm run build` | Production build into `dist/book-tracker-frontend/browser` |
| `npm run watch` | Development build that rebuilds on change |
| `npm test` | Unit tests (Karma + Jasmine, needs Chrome) |

## Configuration

The API base URL is set per build in `src/environments/`:

| File | Used by | `apiBaseUrl` |
| --- | --- | --- |
| `environment.development.ts` | `npm start` | `http://localhost:8080/api/v1` |
| `environment.ts` | `npm run build` | `/api/v1` (same origin, behind a reverse proxy) |

## Features

- **Sign in / register** with inline validation, and an automatic sign-out when the token expires.
- **Library** with status filters (kept in the URL), per-status counts, inline page editing with
  a progress bar, and status changes that apply instantly and roll back on failure.
- **Search** with debounced, cancellable queries; books you already track are flagged.
- Loading skeletons, empty and error states with retry, and toast notifications.
- Keyboard and screen-reader friendly; respects reduced-motion preferences.

## Project layout

```
src/app/
├── core/          Cross-cutting singletons
│   ├── auth/            Session state, token storage, interceptor, guards, redirect checks
│   ├── http/            Error interceptor, error messages, silent-error context token
│   ├── models/          Types mirroring the backend DTOs
│   └── notifications/   Toast service and container
├── features/
│   ├── auth/            Login and register pages
│   ├── library/         Library state, page and book card
│   └── search/          Search service and page
├── layout/        Authenticated shell (header, navigation, main landmark)
└── shared/        Presentational building blocks: icon, spinner, cover, empty state, skeleton
```
