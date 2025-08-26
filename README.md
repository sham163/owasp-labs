# Secure University – OWASP Labs (Java + React)

**Deliberately vulnerable** training app: Spring Boot backend + React frontend,
OWASP Top 10 labs with **easy / medium / hard** levels.  
**Single public port:** `3020` (served by Nginx; `/api` is proxied to backend).

## Prereqs
- Docker + Docker Compose

## Run (one command)
```bash
docker compose up --build
