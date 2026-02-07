# AGENTS.md

Guidelines for coding agents working in this repository (`proginator` / `lernapp`).

## Project Snapshot

- Stack: Spring Boot `3.5.9`, Java `17`, Maven Wrapper (`./mvnw`)
- Main package: `com.example.prog1learnapp`
- Build coordinates: `uni.prog1:lernapp`
- UI: Thymeleaf templates + static CSS/JS
- Security: Spring Security form login
- DB:
  - `dev` profile: H2 in-memory (`jdbc:h2:mem:testdb`)
  - `prod` profile: PostgreSQL (`jdbc:postgresql://localhost:5432/lernapp`)
- Special feature: Java code execution in Docker (`DockerExecutionService`)

## Working Commands

```bash
# Full build
./mvnw clean install

# Run app in development profile (recommended locally)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Run one test class / one test method
./mvnw test -Dtest=LernappApplicationTests
./mvnw test -Dtest=LernappApplicationTests#testDockerExecution
```

IntelliJ VM option for local dev:

```bash
-Dspring.profiles.active=dev
```

## Runtime Dependencies

### PostgreSQL for `prod`

```bash
docker run --name lernapp-postgres \
  -e POSTGRES_DB=lernapp \
  -e POSTGRES_USER=lernapp \
  -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 \
  -d postgres:16
```

### Docker image for code execution sandbox

Build before testing execution endpoints/features:

```bash
./build-docker-image.sh
```

This creates image `proginator-java-sandbox` used by `DockerExecutionService`.

Code execution now uses warm reusable Docker containers (default mode) plus a lightweight custom Java runner (`Prog1InternalTestRunner`) instead of JUnit ConsoleLauncher for lower latency.

## Current Architecture

- Application entrypoint: `src/main/java/com/example/prog1learnapp/Prog1LearnApp.java`
- MVC controllers: `controller/` (`AuthController`, `LearnController`)
- REST controller: `controller/CodeExecutionController`
- Security/config: `config/`
  - `SecurityConfig`, `CustomUserDetailsService`, `GlobalExceptionHandler`
  - `TestUserInitializer` (`@Profile("dev")`)
  - lesson seeders in `config/lessons/**`
- Persistence:
  - entities in `model/`
  - repositories in `repository/`
- Code execution logic: `service/DockerExecutionService`
  - Warm container pool mode (`app.code-execution.mode=warm-container`)
  - Fallback mode: legacy one-container-per-run (`legacy-run`)
  - Per-run performance metrics in API response:
    - `executionDuration`, `dockerStartupMs`, `compileMs`, `testRunMs`
- Templates: `src/main/resources/templates/`
- Static files: `src/main/resources/static/`

## Coding Standards

### Package and naming rules

- Keep Java code under `com.example.prog1learnapp.*`
- Class names: PascalCase
- Methods/fields: camelCase
- Constants: UPPER_SNAKE_CASE
- Prefer meaningful names; avoid abbreviations without context

### Dependency injection

- Prefer constructor injection (already the dominant pattern in this project)
- Avoid field injection unless there is a strong reason

### Logging and output

- Use SLF4J (`LoggerFactory.getLogger(...)`)
- Never use `System.out.println` / `System.err.println` in application code
- Use:
  - `debug` for detailed flow
  - `info` for lifecycle/business events
  - `warn` for recoverable anomalies
  - `error` for failures

### Persistence and transactions

- Use JPA annotations consistently (`@Entity`, `@Table`, `@Column`, `@Lob`, relations)
- Use `@Transactional` for write operations that must be atomic
- When adding seed data, keep initialization idempotent (check existence before insert)

### Validation and error handling

- Validate user input at controller boundaries
- Reuse global handling via `GlobalExceptionHandler` for expected failures
- Return suitable HTTP status codes for REST endpoints

### Security

- Keep BCrypt password encoding
- Preserve route protection model from `SecurityConfig`
- Do not unintentionally expose `/api/execution/**` or exercise completion endpoints
- Keep CSRF decisions explicit when changing form/API flows
- Never commit secrets or passwords
- Use environment variables for sensitive configuration
- Validate all user input
- Use HTTPS in production
- Implement proper session management
- Keep strict Docker isolation for code execution:
  - non-root user
  - `--network=none`
  - `--cap-drop=ALL`
  - `--security-opt=no-new-privileges`
  - read-only rootfs with dedicated writable tmpfs for `/tmp`

## Profile and Config Notes

- `application.yml` contains common app + Thymeleaf + server config
- `application-dev.yml`:
  - H2 console enabled at `/h2-console`
  - `spring.jpa.hibernate.ddl-auto=update`
  - `app.code-execution.docker-enabled=true`
- code execution defaults:
  - `app.code-execution.mode=warm-container`
  - `app.code-execution.pool-size=1`
  - `app.code-execution.warm-image=proginator-java-sandbox`
  - `app.code-execution.default-memory-mb=512`
  - `app.code-execution.default-cpus=2.0`
- `application-prod.yml` currently uses PostgreSQL and `ddl-auto=create`
  - Treat schema strategy changes as a deliberate decision and call them out in PR notes

## Testing Guidance

- Tests live in `src/test/java`
- Current suite is integration-oriented (`@SpringBootTest`)
- When adding features:
  - add focused unit/integration tests for changed behavior
  - prefer deterministic tests (avoid hard dependency on local Docker unless required)
  - if Docker is required, document prerequisites in test comments/PR notes
- For execution performance validation, use the timing fields returned by `/api/execution/run` and compare:
  - setup/transfer (`dockerStartupMs`)
  - compilation (`compileMs`)
  - test execution (`testRunMs`)
  - total (`executionDuration`)

## Frontend and Templates

- Keep Thymeleaf template names aligned with controller returns:
  - `login`, `register`, `dashboard`, `lesson`, `exercise`
  - error pages in `templates/error/404.html` and `templates/error/500.html`
- Keep static assets organized by type under `static/css`, `static/js`, `static/images`

## Agent Change Rules

- Make minimal, targeted edits
- Do not silently refactor broad areas unrelated to the task
- Preserve existing language in user-facing German content unless asked otherwise
- If modifying security, persistence, or execution sandbox behavior, include brief risk notes
