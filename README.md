# Skripty

AI study assistant: upload a PDF, DOCX or your notes and get a summary, a multiple-choice test and flashcards in seconds.

## Features
- Upload PDF / DOCX / TXT files or paste text
- AI-generated summary, quiz with explanations and flip flashcards (Google Gemini API)
- Background generation on a dedicated thread pool, so the app stays responsive while AI works
- Automatic retries and a fallback model when the AI service is overloaded
- JWT authentication, each user sees and manages only their own materials

## Tech stack
| Part | Technologies |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA, MySQL, Apache PDFBox / POI |
| Frontend | JavaScript, React, Vite, React Router, Axios |
| AI | Google Gemini API (structured JSON output) |

## Project structure
```
skripty-backend/    Spring Boot REST API
skripty-frontend/   React single-page app
```

## Running locally

**Backend:** copy `skripty-backend/.env.example` to `skripty-backend/.env`, fill in your values, start MySQL, then run `SkriptyApplication`. The API starts on `http://localhost:8080`.

**Frontend:**
```bash
cd skripty-frontend
npm install
npm run dev
```
Then open `http://localhost:5173`.
