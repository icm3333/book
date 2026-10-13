# Book Tracker

Track a personal reading list. Register, sign in, search books through
[Open Library](https://openlibrary.org), add them to your library and record how far you've read.

The repository holds both halves of the app:

| Part | Location | Stack |
| --- | --- | --- |
| REST API | repository root | Spring Boot 4.1, Spring Security (JWT), Spring Data JPA, PostgreSQL |
| Web client | [`frontend/`](frontend/README.md) | Angular 20 (standalone components, signals), Tailwind CSS v4 |

## Requirements

- Java 17 or newer
- PostgreSQL 13 or newer
- Node.js 20.19+ or 22.12+ (frontend only)
- No Maven install needed — use the bundled wrapper (`mvnw` / `mvnw.cmd`)

## Getting started

### 1. Backend

Create the database:

```sql
CREATE DATABASE book;
```

Copy the environment template and fill in real values:

```bash
cp example.env .env
```

`.env` is git-ignored and loaded automatically at startup.

Run in development mode, which creates and updates the schema from the entities:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API listens on `http://localhost:8080`.

> Real environment variables take precedence over `.env`. If a variable such as `DB_URL` is
> exported in your shell, it overrides the file — check there first when the app connects to an
> unexpected database.

### 2. Frontend

With the API running:

```bash
cd frontend
npm install
npm start
```

Open http://localhost:4200 and create an account. The API already allows this origin through
`CORS_ALLOWED_ORIGINS`. See [`frontend/README.md`](frontend/README.md) for its scripts and
configuration, and [`frontend/CHOICES.md`](frontend/CHOICES.md) for why it is built the way it is.

## Configuration

Backend settings, read from the environment or `.env`:

| Variable | Required | Default | Purpose |
| --- | --- | --- | --- |
| `DB_URL` | no | `jdbc:postgresql://localhost:5432/book` | JDBC URL |
| `DB_USER` | no | `postgres` | Database user |
| `DB_PASSWORD` | yes | — | Database password |
| `JWT_KEY` | yes | — | HMAC signing secret, minimum 32 characters |
| `JWT_EXPIRATION` | yes | — | Token lifetime in milliseconds |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:4200` | Comma-separated browser origins |
| `OPENLIBRARY_BASE_URL` | no | `https://openlibrary.org` | Open Library base URL |
| `OPENLIBRARY_USER_AGENT` | no | `BookTracker/0.0.1` | User-Agent sent upstream |
| `SPRINGDOC_ENABLED` | no | `true` | Serves the OpenAPI description and Swagger UI |
| `TEST_DB_URL` | no | `jdbc:postgresql://localhost:5432/book` | Database used by the tests |

The frontend's API address is set per build in `frontend/src/environments/`.

### Profiles

- **default** — `ddl-auto: validate`, SQL logging off. The app verifies the schema but never
  changes it, so it will not start against an empty database. Intended for anything other than
  local development.
- **dev** — `ddl-auto: update`, SQL logging and debug logs on.

## API documentation

With the API running:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Use the **Authorize** button in Swagger UI to paste a JWT and call the secured endpoints.

Documentation endpoints are public so the contract is easy to explore. Set
`SPRINGDOC_ENABLED=false` before exposing the service on an untrusted network.

## Endpoints

All routes are prefixed with `/api/v1`. Everything except `/auth/**` requires
`Authorization: Bearer <token>`.

### Authentication

| Method | Path | Success | Description |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | `201` | Create an account |
| `POST` | `/auth/login` | `200` | Exchange credentials for a JWT |

### Books and library

| Method | Path | Success | Description |
| --- | --- | --- | --- |
| `GET` | `/books/search?query=` | `200` | Search Open Library (max 20 results) |
| `GET` | `/books/me?status=` | `200` | List the caller's tracked books, optionally filtered |
| `POST` | `/books/me` | `201` | Start tracking a book |
| `GET` | `/books/me/stats` | `200` | Reading statistics for the caller |
| `PATCH` | `/books/me/{userBookId}/progress` | `200` | Update the current page |
| `PATCH` | `/books/me/{userBookId}/status` | `200` | Update the reading status |

`status` is one of `TO_READ`, `READING`, `COMPLETED`.

The library endpoints are scoped to `/me` and resolve the account from the JWT. No endpoint
accepts a user id from the client, and tracking records are always loaded through an
ownership-scoped query — a record belonging to another account is reported as `404` rather than
`403`, so the API does not disclose which ids exist.

### Example

```bash
# Register and log in
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"iris","email":"iris@example.com","password":"s3cret-pass"}'

TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"iris","password":"s3cret-pass"}' | jq -r .token)

# Search, track, then record progress
curl "http://localhost:8080/api/v1/books/search?query=sicp" -H "Authorization: Bearer $TOKEN"

curl -X POST http://localhost:8080/api/v1/books/me \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"openLibraryId":"/works/OL1W","title":"SICP","author":"Harold Abelson","pageCount":657}'

curl -X PATCH http://localhost:8080/api/v1/books/me/1/progress \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"currentPage":120}'
```

## Errors

Every failure returns the same shape:

```json
{
  "timestamp": "2026-01-15T10:30:00.123",
  "status": 404,
  "error": "Not Found",
  "message": "Tracking record not found with id: 42",
  "path": "/api/v1/books/me/42/progress"
}
```

| Status | Raised when |
| --- | --- |
| `400` | Validation failure or invalid argument |
| `401` | Missing, invalid or expired token; bad credentials |
| `404` | Resource missing, or owned by another account |
| `409` | Username or email already taken; book already tracked |
| `502` | Open Library unreachable or returned an error |
| `500` | Unexpected failure (details are logged, never returned) |

The frontend shows the `message` field to the user, so it is written to be read by people.

## Tests

Backend:

```bash
./mvnw test
```

Covers the service layer, both controllers (routing, validation, error mapping, authentication
and cross-user access), the Open Library client against a stubbed transport, the OpenAPI
description, and application context startup. The tests need a reachable PostgreSQL database and
manage their own schema; point them elsewhere with `TEST_DB_URL`.

Frontend (needs Chrome):

```bash
cd frontend
npm test
```

Covers redirect validation after sign-in, error-message selection, token attachment, and the
library's optimistic updates and rollback.

## Troubleshooting

**Registration fails with `null value in column "id" of relation "roles"`.** The `roles` table
was created outside Hibernate, so its `id` column isn't auto-generated. `ddl-auto: update` never
alters existing columns, so it won't repair this on its own. Check which tables are affected:

```sql
SELECT table_name, is_identity
FROM information_schema.columns
WHERE table_schema = 'public' AND column_name = 'id';
```

For each table showing `NO`, add the identity and start it after the existing ids:

```sql
ALTER TABLE roles ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM roles;
```

**The frontend says it cannot reach the server.** Check that the API is running on port 8080 and
that `CORS_ALLOWED_ORIGINS` includes the address the frontend is served from.

## Known limitations

- `POST /books/me` does not accept a cover URL, so tracked books show a generated placeholder in
  the library even when search found a cover.
- There is no endpoint for removing a book from the library.
- The schema is managed by Hibernate rather than migration scripts.
- The frontend has no statistics page yet, although `/books/me/stats` is available.

## Project layout

```
.
├── src/main/java/rcm/book/   Backend
│   ├── client/                 Open Library integration and its RestClient configuration
│   ├── config/                 Security, JWT, CORS and OpenAPI configuration
│   ├── controller/             REST endpoints
│   ├── dto/                    Request and response payloads
│   ├── exception/              Domain exceptions and the global handler
│   ├── model/                  JPA entities and enums
│   ├── repository/             Spring Data repositories
│   └── service/                Business logic
├── src/main/resources/       application.yaml and the dev profile
├── src/test/                 Backend tests
├── frontend/                 Angular client (see frontend/README.md)
├── example.env               Template for the backend's .env
└── plans.md                  Refactor plan and deferred work
```
