# Exploration: setup-github-ci-cd

## Current State

The repository has **no CI/CD automation** today. There is no `.github/workflows/` directory, no `Dockerfile`, and no release or security scanning automation. The backend is a Spring Boot 3.3.0/Java 21 Maven project (`backend/pom.xml`) with a mature test suite (265 unit + 48 integration/invariant tests), JaCoCo coverage, and Spotless formatting already wired in Maven.

Key local tooling already available:

- `mvn test` runs unit tests on H2 via `application-test.yml`.
- `mvn verify -DskipITs=false` runs integration/invariant tests, some of which require Docker (Testcontainers MySQL).
- `mvn spotless:check` enforces `google-java-format` AOSP style.
- `mvn jacoco:check` enforces **80% LINE coverage** at the bundle level.
- `application.yml` is fully environment-driven (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `SERVER_PORT`, `CORS_ORIGINS`).

Notable gaps:

- `README.md` references `./mvnw`, but no Maven wrapper exists in `backend/`.
- No `Dockerfile` or `docker-compose.yml` exists, so there is no artifact to deploy or container to run integration tests in.
- `openspec/config.yaml` lists `coverage_threshold: 0.10`, while `pom.xml` enforces `0.80`. The effective CI gate should align with `pom.xml` unless the team intentionally lowers it.

## Affected Areas

- `.github/workflows/ci.yml` — new; main pull-request and push validation pipeline.
- `.github/workflows/release.yml` — new; optional build/push of Docker image and GitHub Release.
- `.github/dependabot.yml` — new; automated dependency updates.
- `.github/workflows/codeql.yml` — new; optional static security analysis.
- `backend/Dockerfile` — new; required for any container-based deployment or release workflow.
- `backend/mvnw` + `.mvn/wrapper/` — new or restored; aligns README with reality and removes Maven-install dependency in CI.
- `backend/pom.xml` — may need profile/tweak for CI (e.g., `-DskipITs=false`, coverage gate tuning, Docker skip flags).
- `README.md` — update CI badge and documented commands if wrapper or commands change.
- `openspec/config.yaml` — reconcile `coverage_threshold` with actual JaCoCo gate.

## CI/CD Requirements and Priorities

### Must-have for first slice (CI)

1. Compile with Java 21 on every PR and push to `main`.
2. Run unit tests (`mvn test`) and fail on failure.
3. Run Spotless check (`mvn spotless:check`).
4. Run JaCoCo report; surface coverage artifact (do **not** fail PRs on the 80% gate until baseline is understood).
5. Cache Maven dependencies (`~/.m2`) between runs.
6. Run integration tests (`mvn verify -DskipITs=false`) with Docker service enabled; tests tagged `@EnabledIfDockerAvailable` will skip themselves if Docker socket is unreachable.
7. Pull-request trigger on `main`; push trigger on `main`.

### Should-have next (quality gates)

1. Align JaCoCo coverage gate: decide whether CI enforces `0.80` (current `pom.xml`) or a staged threshold.
2. Branch protection rule requiring the CI check to pass before merge.
3. Add `paths` filters so backend-only changes do not trigger unnecessary CI runs on unrelated files.
4. Add Maven wrapper to make CI reproducible without a pre-installed Maven version.

### Could-have later (CD / security / release)

1. `Dockerfile` and `release.yml` to build and push an image to GitHub Container Registry or Docker Hub.
2. `dependabot.yml` for Maven and GitHub Actions ecosystems.
3. `codeql.yml` for Java static analysis.
4. Semantic-release or tag-based release workflow.
5. Deployment job to a cloud target (requires environment secrets and target decision).

## Approaches

### 1. Minimal CI-only slice

Create a single `.github/workflows/ci.yml` that compiles, runs unit tests, Spotless, and JaCoCo report. Integration tests are left out of the first PR to keep the change small.

- Pros: Fast to review, low risk, immediate value for every PR.
- Cons: Integration tests still run only locally; Docker-dependent coverage may drop in CI.
- Effort: Low

### 2. CI with Docker-enabled integration tests (recommended)

Create `.github/workflows/ci.yml` with `services:` or `runner docker` setup, run `mvn -B verify -DskipITs=false spotless:check`. Add a `Dockerfile` only if the team also wants the release slice.

- Pros: Matches local `verify` behavior; gives confidence that Docker-dependent invariant tests pass; uses existing `@EnabledIfDockerAvailable` guards.
- Cons: Slightly longer build times (~2-4 min container startup); requires Docker socket in GitHub runners (available on `ubuntu-latest`).
- Effort: Medium

### 3. Full CI/CD + security stack

Add CI, release workflow with image push, Dependabot, CodeQL, and deployment job.

- Pros: Complete pipeline, production-ready.
- Cons: Requires decisions on registry, deploy target, secrets, and environment names; much larger than 400-line PR budget.
- Effort: High

## Recommendation

Start with **Approach 2: CI with Docker-enabled integration tests**. It is the smallest slice that validates the existing `mvn verify -DskipITs=false spotless:check` command the README already documents, and it does not require deployment decisions. Keep the first PR under the 400-line review budget by scoping it to:

- `.github/workflows/ci.yml`
- `backend/Dockerfile` (optional in first slice; include only if release workflow is also requested)
- `backend/mvnw` + `.mvn/wrapper/` restoration
- Minor `README.md` updates
- `openspec/config.yaml` coverage threshold alignment

Do **not** include release, deploy, CodeQL, or Dependabot in the first PR; they can be chained once CI is stable.

## Risks

1. **Coverage gate mismatch**: `pom.xml` enforces 80% LINE coverage. Running `verify` in CI will fail if the project is currently below 80%, or if Docker-dependent tests are skipped and coverage drops. Mitigation: first PR should run JaCoCo `report` but disable the `check` execution in CI (`-Djacoco.skip=true` or a CI profile), then raise the gate in a follow-up once baseline is confirmed.
2. **Maven wrapper missing**: CI must either install a specific Maven version or the wrapper must be added. Adding the wrapper touches many files and can bloat the PR; it should be a separate small PR or accepted as a generated-code exception.
3. **Docker availability assumption**: GitHub-hosted `ubuntu-latest` runners have Docker, but self-hosted runners might not. The `@EnabledIfDockerAvailable` guard handles this gracefully by skipping tests, which means CI will pass but silently skip critical invariant tests on those runners.
4. **No deployment target defined**: Continuous Deployment cannot be designed without knowing the target (ECS, Kubernetes, Fly.io, Render, VM, etc.) and registry (GHCR, Docker Hub, ECR).
5. **Secrets and environment protection**: Any CD workflow requires `JWT_SECRET`, DB credentials, registry tokens, and possibly cloud credentials. These must be created as GitHub secrets before CD is implemented.

## Open Decisions

1. Should the first PR include the Maven wrapper, or should CI install Maven 3.9+ explicitly?
2. Should CI enforce the 80% JaCoCo gate immediately, or report coverage without failing?
3. Should a `Dockerfile` be added in the first slice, or deferred until the release workflow is planned?
4. What is the target registry and deployment platform for CD?
5. Are branch protection rules already enabled on `main`, or should they be recommended as part of this change?

## Suggested First Slice for Implementation

1. Add `backend/mvnw` Maven wrapper (or document that CI installs Maven 3.9+).
2. Create `.github/workflows/ci.yml`:
   - Trigger: `pull_request` and `push` to `main`.
   - OS: `ubuntu-latest`.
   - Java: `21` (Eclipse Temurin).
   - Steps: checkout, setup Java, cache Maven, `mvn -B -f backend/pom.xml verify -DskipITs=false spotless:check jacoco:report`.
   - Upload `backend/target/site/jacoco/` and surefire/failsafe reports as artifacts.
3. Add `backend/Dockerfile` (multi-stage build with Java 21) if release is in scope; otherwise defer.
4. Update `README.md` to reference real commands and add CI status badge placeholder.
5. Reconcile `openspec/config.yaml` `coverage_threshold` with `pom.xml`.
6. Recommend branch protection: require the CI job to pass before merge.

This first slice is autonomous, verifiable, and keeps the review within the 400-line budget if the Maven wrapper is treated as a generated-code exception or delivered in its own PR.
