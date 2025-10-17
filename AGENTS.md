# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/notes/keepapi` contains Spring Boot controllers (`controller`), services (`service`), repositories (`repository`), and data models (`model`). Mirror this layout when adding new features.
- `src/main/resources/application.properties` holds runtime configuration; add profile-specific overrides (e.g., `application-dev.properties`) for local secrets.
- `build.gradle`, `gradlew*`, and the `gradle/` wrapper pin dependency versions—update via the wrapper task rather than manual edits.

## Build, Test, and Development Commands
- `./gradlew bootRun` starts the API on the default Spring port for interactive development.
- `./gradlew build` compiles sources, runs tests, and produces the distributable JAR in `build/libs/`.
- `./gradlew test` executes the JUnit 5 suite; run it before pushing changes.
- `./gradlew clean` clears the `build/` directory when you need a fresh build.

## Coding Style & Naming Conventions
- Target Java 17 with Spring Boot 3.2; use 4-space indentation and Lombok annotations for boilerplate.
- Keep package names feature-based (`controller`, `service`, `repository`, `model`); DTOs belong in the feature they support.
- Name classes in PascalCase, methods and variables in camelCase, and constants in SCREAMING_SNAKE_CASE.
- REST endpoints should remain lowercase plural nouns (e.g., `/api/notes`), and log meaningful context through `Slf4j`.

## Testing Guidelines
- Place tests in `src/test/java/com/notes/keepapi/...`, mirroring the production package tree.
- Use Spring Boot Test + JUnit 5; prefer MockMvc for controllers and slice tests (e.g., `@DataJpaTest`) for data access.
- Cover business rules (note creation, checklist toggling, prediction edge cases) and keep tests deterministic and independent.
- Name tests descriptively (`shouldToggleChecklistItem`) and assert both happy-path and failure scenarios.

## Commit & Pull Request Guidelines
- Follow short, imperative commit subjects (`Add prediction`, `Fix checklist toggle`) aligned with the existing history.
- Group related work per commit and keep diffs focused; large features can use stacked PRs with clear dependencies.
- Pull requests must include context, testing evidence (`./gradlew test` output), linked issues, and screenshots or sample payloads when APIs change.

## Environment & Configuration Tips
- Default settings live in `application.properties`; prefer `application-<profile>.properties` or environment variables for machine-specific credentials.
- Document any new infrastructure or external integrations within the PR and update onboarding docs when dependencies change.
