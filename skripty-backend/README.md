# Skripty — Backend

AI study assistant backend: upload study material (PDF/DOCX/pasted text) and get an
auto-generated summary, multiple-choice test and flashcards, powered by the Gemini API.

## Tech stack

- Java 17, Spring Boot 3
- Spring Security + JWT (stateless auth)
- Spring Data JPA + MySQL
- Apache PDFBox / POI (file text extraction)
- Google Gemini API (AI generation)
- Async thread pool for AI calls, so multiple users can generate content concurrently
  without blocking each other or the web server

## Running locally

1. Make sure MySQL is running locally (the app auto-creates the `skripty` database on
   first run — `createDatabaseIfNotExist=true` in `application.yml`).
2. Copy `.env.example` to `.env` and fill in your own values (a `.env` with working
   values for local development already exists if you got this project from Katarina —
   it is git-ignored and never committed).
3. Load the `.env` file into your shell before running (IntelliJ: use the EnvFile plugin,
   or a run configuration with "Environment variables" pointing at `.env`). From a plain
   terminal:
   ```bash
   export $(grep -v '^#' .env | xargs)
   ./mvnw spring-boot:run
   ```
4. The API starts on `http://localhost:8080`.

## API overview

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | — | Create an account |
| POST | `/api/auth/login` | — | Get a JWT |
| POST | `/api/documents/upload` | Bearer token | Upload a PDF/DOCX/TXT file |
| POST | `/api/documents/paste` | Bearer token | Submit pasted text instead of a file |
| GET | `/api/documents/{id}` | Bearer token | Poll generation status / get results |
| GET | `/api/documents` | Bearer token | List your past documents |

Upload/paste endpoints return immediately with `status: PENDING` — generation runs in
the background. The frontend polls `GET /api/documents/{id}` every couple of seconds
until `status` becomes `READY` (or `FAILED`).
