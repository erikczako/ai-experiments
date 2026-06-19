# Codex Spring + DynamoDB Local experiment

Four iterations of asking OpenAI Codex to produce a senior-level Spring Boot
microservice backed by DynamoDB, running against DynamoDB Local (no Docker).
Each subdirectory is the unedited output of one prompt, kept side-by-side so the
results can be compared.

The brief was the same across all four: a small REST shopping-cart service,
DynamoDB as the only store, layered architecture, tests, formatting, and a
coverage gate. Each prompt tightened the constraints or added a requirement.

## The four runs

| Dir | Prompt focus |
| --- | --- |
| [`codex-dynamodb-local-spring-1`](codex-dynamodb-local-spring-1/) | First pass. Build a cart service on DynamoDB Local, layered packages, tests, JaCoCo gate at 85%. |
| [`codex-dynamodb-local-spring-2`](codex-dynamodb-local-spring-2/) | Re-run with stricter expectations: Maven wrapper, Spring profiles, separate repository/service layers. |
| [`codex-dynamodb-local-spring-3`](codex-dynamodb-local-spring-3/) | Adds HTTP Basic auth and scopes the cart to the authenticated user. DynamoDB Local started via `exec:java` instead of a shell script. |
| [`codex-dynamodb-local-spring-4`](codex-dynamodb-local-spring-4/) | Targets Spring Boot 4.1. Drops the auth requirement in favour of an `X-User-Id` header so the focus stays on the persistence and config wiring. |

## What stayed consistent

- Single DynamoDB table with `userId` (partition key) and `productId` (sort key).
- Conditional put to reject duplicate products atomically (`409 Conflict`).
- Bean validation on the request bodies (`400 Bad Request` for invalid input).
- `mvn verify` runs Spring Java Format and enforces 85% line coverage via JaCoCo.
- No Docker. DynamoDB Local runs on `http://localhost:8000`.

## What changed between runs

- **Package layout** drifted from `api`/`domain`/`infrastructure` (run 1) to
  `api`/`service`/`repository` (run 2) to `web`/`service`/`persistence` (run 3)
  to `web`/`service`/`repository`/`domain`/`config` (run 4).
- **Profiles** appeared in run 2 (`local` vs default credential chain) and were
  carried forward in runs 3 and 4.
- **Security** only appears in run 3 (HTTP Basic, per-user cart scoping). Run 4
  drops auth entirely and identifies the user via an `X-User-Id` header.
- **DynamoDB Local bootstrap** moved from a downloaded tarball + shell script
  (runs 1 and 2) to a Maven `exec:java` invocation that reuses the test
  classpath (run 3) to an in-process `@Bean` started by the Spring context on
  the `local` profile (run 4) — no external process at all.
- **Spring Boot version** stayed on the 3.x line through runs 1–3 and jumped to
  Spring Boot 4.1 in run 4.
- **Testing depth** grew: run 1 has focused unit tests, run 3 adds a full
  integration test that boots Spring Boot against an in-memory DynamoDB Local,
  and run 4 carries that forward with a reusable `DynamoDbLocalTestSupport`
  base class that wires the embedded server via `@DynamicPropertySource`.

## Running any of them

Each project has its own README with exact commands. The shape is the same:

```bash
cd codex-dynamodb-local-spring-<n>

# Terminal 1 — start DynamoDB Local
./scripts/run-dynamodb-local.sh        # runs 1 and 2
# or the exec:java incantation in run 3's README
# run 4 does not need this — the local profile starts DynamoDB Local in-process

# Terminal 2 — start the service
./mvnw spring-boot:run                 # runs 2 and 3 (use the local profile)
mvn  spring-boot:run -Dspring-boot.run.profiles=local  # run 4
mvn  spring-boot:run                   # run 1

# Build, format check, coverage gate
./mvnw verify
```
