# GitHub CI/CD Specification

## Purpose

Define the GitHub Actions CI/CD pipeline, container build, dependency automation, and documentation updates for the Tacosoft backend.

## Requirements

### Requirement: CI Workflow

The repository MUST provide a CI workflow that compiles, tests, format-checks, and reports coverage on every pull request and push to `main`.

| File | `.github/workflows/ci.yml` |
|------|---------------------------|
| Triggers | `pull_request`, `push` to `main` |
| Runner | `ubuntu-latest` |
| JDK | Eclipse Temurin 21 |
| Maven | `backend/mvnw` |
| Cache | `~/.m2` via `actions/cache` |
| Docker | Enabled for Testcontainers |
| Command | `./mvnw -B verify -DskipITs=false spotless:check jacoco:report` from `backend/` |
| Artifacts | `jacoco-report` (`target/site/jacoco/`), `test-reports` (`target/surefire-reports/`, `target/failsafe-reports/`) |

#### Scenario: PR validation succeeds

- GIVEN a pull request targets `main`
- WHEN the CI workflow runs
- THEN compilation, tests, Spotless check, and JaCoCo report complete
- AND the workflow fails on compile/test/Spotless failure

#### Scenario: Coverage does not block merge

- GIVEN a build produces less than 80% line coverage
- WHEN the CI workflow completes
- THEN the workflow MUST NOT fail due to coverage percentage alone

### Requirement: Maven Wrapper

The backend MUST provide a Maven wrapper so CI and local builds use Maven 3.9.6 without a global install.

| Files | `backend/mvnw`, `backend/.mvn/wrapper/maven-wrapper.properties`, `backend/.mvn/wrapper/maven-wrapper.jar` |
| Maven version | 3.9.6 |
| Permissions | `backend/mvnw` MUST be executable on Unix runners |

#### Scenario: Wrapper reports correct version

- GIVEN the repository is checked out on `ubuntu-latest`
- WHEN the CI step runs `./mvnw -version`
- THEN it reports Maven 3.9.6

### Requirement: Production Dockerfile

The backend MUST provide a multi-stage Dockerfile that builds and runs the application as a non-root user.

| File | `backend/Dockerfile` |
| Build stage | `eclipse-temurin:21-jdk-alpine` |
| Build command | `./mvnw -B package -DskipTests` |
| Runtime stage | `eclipse-temurin:21-jre-alpine` |
| User | Non-root |
| Port | `8080` |
| Entrypoint | `java -jar app.jar` |
| Runtime config | `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `SERVER_PORT`, `CORS_ORIGINS` |

#### Scenario: Image builds and starts securely

- GIVEN the source code is checked out
- WHEN Docker builds `backend/Dockerfile`
- THEN the image compiles the project and exposes port `8080`
- AND the container runs as a non-root user

### Requirement: Release Workflow

The repository MUST provide a release workflow that builds and pushes a container image to GHCR on semantic version tags.

| File | `.github/workflows/release.yml` |
| Trigger | Tags matching `v*.*.*` |
| Auth | `GITHUB_TOKEN` to GHCR |
| Image | `ghcr.io/{OWNER}/tacosoft-backend:{TAG}` |

#### Scenario: Tag push publishes image

- GIVEN a tag `v1.2.3` is pushed
- WHEN the release workflow runs
- THEN it builds the backend image with Docker Buildx
- AND pushes it to GHCR with the tag `v1.2.3`

### Requirement: Dependabot Configuration

The repository MUST configure Dependabot to check for Maven and GitHub Actions updates weekly.

| File | `.github/dependabot.yml` |
| Maven | `backend/pom.xml`, weekly |
| GitHub Actions | `.github/workflows/`, weekly |

#### Scenario: Weekly update scan

- GIVEN Dependabot is enabled in the repository
- WHEN the weekly schedule triggers
- THEN Dependabot opens PRs for outdated Maven and GitHub Actions dependencies

### Requirement: README Updates

The README MUST reflect CI status and wrapper-based commands.

#### Scenario: README documents CI and Docker

- GIVEN the README is viewed
- THEN it contains a CI status badge placeholder
- AND Maven command examples use `./mvnw`
- AND a Docker run example is present

### Requirement: OpenSpec Coverage Threshold

The OpenSpec config MUST align the coverage threshold with `pom.xml`.

#### Scenario: Threshold matches build

- GIVEN `openspec/config.yaml` is loaded
- THEN `verify.coverage_threshold` equals `0.80`
