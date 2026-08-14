# CodingArena server

The Ktor API owns authentication, classrooms, assignments, progress snapshots,
hidden tests, and code-execution orchestration. The client never receives the
Judge0 token or hidden test inputs.

## Local requirements

- JDK 17
- PostgreSQL 17 (or `compose.server.yml`)
- A private Judge0 CE instance. Use the official pinned release and its supplied
  `docker-compose.yml`; do not expose Judge0 publicly. Point `JUDGE0_URL` at its
  private address and enable `X-Auth-Token` in Judge0 configuration.

Copy `.env.example` into your environment, then run:

```bash
./gradlew :server:run
```

The service initializes its PostgreSQL tables idempotently. `GET /health`
returns `{"status":"ok"}` once the API is ready.

## Security boundaries

- Source is capped at 64 KB, output at 8 KB, and Judge0 jobs at 5 CPU seconds,
  10 wall seconds, and 256 MB.
- Runner networking is disabled on every submission.
- JWTs expire after seven days; production must use a random secret.
- Teachers can only read classes in which they are the teacher. Students can
  only read their own submissions and classes they joined.
- Public results contain example inputs; hidden results contain counts only.

