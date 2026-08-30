# myUON Server

Ktor backend for the myUON application.

## Package Architecture

- `domain/` — Pure Kotlin models (`StudentProfile`), repo interfaces (`StudentRepository`), and business logic (`AuthService`). Zero Ktor-server and Exposed imports.
- `db/` — Database layer (Exposed tables and repository implementations against Neon Lakebase Postgres).
- `infra/` — Outbound client communication with the Neon Auth hosted REST API.
- `routes/` — Ktor routing and HTTP endpoints (`/health`, `/auth/signup`, `/auth/login`).

## Running Locally

1. Copy `.env.example` to `.env` and supply your database credentials.
2. Export variables or pass them at runtime.
3. Run:
   ```bash
   ./gradlew :server:run
   ```

## Running Tests

All unit and integration tests run against in-memory H2 (Postgres-compatibility mode) and mocked HTTP engines. No live internet connection is needed for tests:
```bash
./gradlew :server:test
```
