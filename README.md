# Smart-Email-Reply-Generator
An AI powered email reply generator.

## Overview
A Spring Boot backend that generates context-aware email replies using **Groq** (default) or **Google Gemini**, with optional **Tavily** web search to ground replies in up-to-date info. A Chrome extension adds an "AI Reply" button inside Gmail.

## Project structure
- `smart-reply-api/` – Spring Boot backend (Java)
- `smart-reply-web/` – React + Vite frontend
- `smart-reply-extension/` – Gmail Chrome extension
- `hello-world-ext/` – extension starter demo

## Configuration (environment variables)
| Variable | Purpose | Default |
|---|---|---|
| `LLM_PROVIDER` | `groq` or `gemini` | `groq` |
| `GROQ_API_KEY` | Groq API key | – |
| `GROQ_MODEL` | Groq model | `llama-3.3-70b-versatile` |
| `GEMINI_URL` / `GEMINI_KEY` | Gemini endpoint / key (optional) | – |
| `TAVILY_API_KEY` | Tavily key (enables web search) | – |
| `TAVILY_DEFAULT_ENABLED` | Use Tavily on every request | `false` |

## Run
```bash
cd smart-reply-api
export GROQ_API_KEY=your_key
export TAVILY_API_KEY=your_key   
./mvnw spring-boot:run
```

## API
`POST /api/email/generate`
```json
{
  "emailContent": "…original email…",
  "tone": "professional",
  "provider": "groq",
  "useSearch": true
}
```
`provider` and `useSearch` are optional; they override the defaults above. Returns the reply as plain text.

## Technologies
Java, Spring Boot, WebClient · Groq (Llama 3.3) / Gemini · Tavily Search API · React, Vite, MUI · JavaScript Chrome extension
