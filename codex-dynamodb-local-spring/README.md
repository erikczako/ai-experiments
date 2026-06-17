# Codex Spring + DynamoDB Local experiment

Three iterations of asking OpenAI Codex to produce a senior-level Spring Boot
microservice backed by DynamoDB, running against DynamoDB Local (no Docker).
Each subdirectory is the unedited output of one prompt, kept side-by-side so the
results can be compared.

The brief was the same across all three: a small REST shopping-cart service,
DynamoDB as the only store, layered architecture, tests, formatting, and a
coverage gate. Each prompt tightened the constraints or added a requirement.

## The three runs

| Dir | Prompt focus | Notable result |
| --- | --- | --- |
| [`codex-dynamodb-local-spring-1`](codex-dynamodb-local-spring-1/) | First pass. Build a cart service on DynamoDB Local, layered packages, tests, JaCoCo gate at 85%. | Java 21, `api`/`domain`/`infrastructure` packages, env-var configuration, downloads DynamoDB Local via a shell script. |
| [`codex-dynamodb-local-spring-2`](codex-dynamodb-local-spring-2/) | Re-run with stricter expectations: Maven wrapper, Spring profiles, separate repository/service layers. | Java 25, `local` profile creates the table, `default` profile uses the AWS default credential chain, dedicated `DynamoDbTableInitializer`. |
| [`codex-dynamodb-local-spring-3`](codex-dynamodb-local-spring-3/) | Adds HTTP Basic auth and scopes the cart to the authenticated user. DynamoDB Local started via `exec:java` instead of a shell script. | `web`/`service`/`persistence` packages, Spring Security config, integration test that boots the full app against an in-memory DynamoDB Local. |

## What stayed consistent

- Single DynamoDB table with `userId` (partition key) and `productId` (sort key).
- Conditional put to reject duplicate products atomically (`409 Conflict`).
- Bean validation on the request bodies (`400 Bad Request` for invalid input).
- `mvn verify` runs Spring Java Format and enforces 85% line coverage via JaCoCo.
- No Docker. DynamoDB Local runs on `http://localhost:8000`.

## What changed between runs

- **Package layout** drifted from `api`/`domain`/`infrastructure` (run 1) to
  `api`/`service`/`repository` (run 2) to `web`/`service`/`persistence` (run 3).
- **Profiles** appeared in run 2 (`local` vs default credential chain) and were
  carried forward in run 3.
- **Security** only appears in run 3 (HTTP Basic, per-user cart scoping).
- **DynamoDB Local bootstrap** moved from a downloaded tarball + shell script
  (runs 1 and 2) to a Maven `exec:java` invocation that reuses the test
  classpath (run 3).
- **Testing depth** grew: run 1 has focused unit tests, run 3 adds a full
  integration test that boots Spring Boot against an in-memory DynamoDB Local.

## Running any of them

Each project has its own README with exact commands. The shape is the same:

```bash
cd codex-dynamodb-local-spring-<n>

# Terminal 1 — start DynamoDB Local
./scripts/run-dynamodb-local.sh        # runs 1 and 2
# or the exec:java incantation in run 3's README

# Terminal 2 — start the service
./mvnw spring-boot:run                 # runs 2 and 3 (use the local profile)
mvn  spring-boot:run                   # run 1

# Build, format check, coverage gate
./mvnw verify
```
