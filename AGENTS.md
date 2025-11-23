# Repository Guidelines

## Project Structure & Module Organization

- Core code lives in `src/main/java/com/sgivu/purchasesale`: `controller` (REST APIs), `service` + `impl`, `repository`, `mapper` (MapStruct), `dto`, `entity`, `security`, `config`, `specification`, `exception`, and `client` integrations.
- Shared resources sit in `src/main/resources`: `application.yaml`, `database/schema.sql` + `data.sql`, and web assets under `static`/`templates`.
- Tests are in `src/test/java/com/sgivu/purchasesale` with sample service tests and `SgivuPurchaseSaleApplicationTests` for bootstrap coverage.
- Docker artifacts are at the repo root (`Dockerfile`), with Maven wrapper scripts `mvnw`/`mvnw.cmd`.

## Build, Test, and Development Commands

```bash
./mvnw clean package          # full build with tests
./mvnw test                   # run unit/integration tests
./mvnw spring-boot:run        # start locally (set SPRING_PROFILES_ACTIVE=dev/local)
./mvnw clean package -DskipTests   # faster packaging for Docker images
docker build -t sgivu-purchase-sale .   # container image
```

## Coding Style & Naming Conventions

- Java 21, Spring Boot 3, Lombok for boilerplate, MapStruct for mappings.
- Use 2-space indentation and prefer constructor injection. Keep controllers thin; business logic belongs in services; persistence only in repositories/specifications.
- DTOs follow `*Request`/`*Response`; entities are singular nouns; mappers named `*Mapper`.
- REST endpoints stay under `/v1/purchase-sales` with lowercase, hyphen-separated paths.
- Favor checked validation (`@Valid`, Jakarta constraints) at API boundaries; surface meaningful messages in exceptions.

## Testing Guidelines

- Framework: Spring Boot Test (JUnit 5). Place tests alongside package equivalents and name them `*Test`.
- Cover service logic and critical authorization paths; mock external clients where possible.
- For repository/specification changes, add slice tests or verify query predicates via in-memory database when feasible.
- Run `./mvnw test` before any PR; include coverage of new error branches when adjusting exceptions.

## Commit & Pull Request Guidelines

- Commit messages follow Conventional Commit styles seen in history (`chore: ...`, `refactor: ...`). Use English or Spanish, but keep the `type: summary` pattern.
- PRs should include: a concise description, linked issue/ticket, steps to reproduce/fix, and test evidence (`./mvnw test` output or logs). For API changes, add sample requests/responses and note new headers.
- Call out config or data changes (env vars, `application*.yml`, `database/schema.sql`) and provide rollback notes if applicable.

## Security & Configuration Tips

- Keep secrets out of the repo; supply `SERVICE_INTERNAL_SECRET_KEY`, datasource credentials, and service URLs via environment or Config Server.
- To exercise internal calls, include header `X-Internal-Service-Key` with the configured key; normal access requires valid JWTs with `rolesAndPermissions`.
