# Repository Guidelines

## Project Structure & Module Organization
- Core infrastructure lives in `infra`, `database`, `parser`, `kernel`, and `mode`; each subtree follows the `src/main/java` and `src/test/java` layout.
- Integration entry points sit in `jdbc`, `jdbc-dialect`, and `proxy`; agent tooling is under `agent`.
- Feature implementations (sharding, encryption, read/write-splitting, etc.) are under `features`, while runnable examples reside in `examples`.
- Shared docs and release assets are in `docs` and `distribution`; never edit generated files under `target`.

## Build, Test, and Development Commands
- `./mvnw clean install -B -T1C -Pcheck` — canonical full build with Spotless, license, and checkstyle gates.
- `./mvnw test -pl {module}` — run unit tests for a specific module; replace `{module}` with `kernel`, `infra`, etc.
- `./mvnw spotless:apply -Pcheck` — format sources; run before committing.
- `./mvnw test jacoco:check@jacoco-check -Pcoverage-check` — enforce Jacoco thresholds across modules.

## Coding Style & Naming Conventions
- Java code uses 4-space indentation, PascalCase for classes, camelCase for members, and UPPER_SNAKE for constants.
- Keep methods focused, avoid duplicate logic, and align abstractions with module boundaries.
- Always include the ASF license header and rely on Lombok only where the module already does.
- Spotless/Checkstyle configurations in `pom.xml` are authoritative; do not hand-format beyond those rules.

## Testing Guidelines
- Prefer JUnit 5 with Mockito; follow the `ClassNameTest` pattern inside the matching `src/test/java` package.
- Test method names use `assertXyz` to describe expectations, and tests should arrange-act-assert explicitly.
- Mock external systems (databases, network, time) but construct POJOs directly.
- Investigate coverage reports under `{module}/target/site/jacoco/index.html` when Jacoco fails.

## Commit & Pull Request Guidelines
- Commit messages follow the `Module: Summary` style (e.g., `kernel: refine route planner`) and describe the intent, not the mechanics.
- Each PR must describe motivation, list key changes, mention affected modules, and link tracking issues.
- Provide reproduction steps for bug fixes, plus config snippets or screenshots when behavior changes.
- Keep PRs focused; split refactors and features, and ensure CI passes before requesting review.

## Security & Configuration Tips
- Never commit secrets; use environment variables or the provided config samples under `examples`.
- Validate new configuration knobs in both standalone and cluster (`mode`) settings, and document default values in `docs`.
