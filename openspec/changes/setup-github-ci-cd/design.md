# Design: Setup GitHub Actions CI/CD for Tacosoft Backend

## Technical Approach

Add GitHub Actions workflows, a Maven wrapper, a multi-stage Dockerfile, and Dependabot. CI validates PRs/pushes with `./mvnw verify`, reporting coverage without failing on percentage. A `CI=true`-activated Maven profile disables only the JaCoCo 80% gate in CI; local builds keep enforcing it. Releases build and push the backend image to GHCR on `v*.*.*` tags, preserving the leading `v` via `docker/metadata-action`. Work is delivered in three chained PRs to respect the 400-line review budget.

## Architecture Decisions

| Decision | Option | Tradeoff | Rationale |
|---|---|---|---|
| Coverage gate in CI | `CI=true` Maven profile skips `jacoco:check` | One `pom.xml` change; local `mvn verify` still enforces 80% | Spec requires exact command; project currently passes 80% locally |
| Release tagging | `docker/metadata-action@v5` with `type=ref,event=tag` | Keeps `v` prefix; no shell interpolation | Safer than composing tags in a shell `run:` block |
| Release auth | `GITHUB_TOKEN` via `docker/login-action` | No long-lived secret; needs `packages:write` | Standard GitHub Actions pattern |
| PR split | 3 chained PRs (wrapper → CI/config → Docker/release) | More coordination than one PR | Respects 400-line budget and auto-forecast strategy |
| Docker base images | Keep mutable `eclipse-temurin:21-{jdk,jre}-alpine` tags | Supply-chain drift risk | Acceptable for current scope; pin to digest later if reproducibility is required |

## Data Flow

```
Developer push/PR ──→ .github/workflows/ci.yml
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
 actions/checkout      actions/setup-java      ./mvnw verify
 (persist-credentials:    (cache: maven)        spotless:check
       false)                                    jacoco:report
        │                     │                     │
        └─────────────────────┬─────────────────────┘
                              ▼
                   actions/upload-artifact
                              │
                              ▼
                  jacoco-report + test-reports

Tag push v*.*.* ──→ .github/workflows/release.yml
                              │
                              ▼
   docker/metadata-action ──→ docker/build-push-action ──→ GHCR
          type=ref,event=tag         push: true
                              │
                              ▼
      ghcr.io/{OWNER}/tacosoft-backend:{TAG}
```

## File Changes

| File | Action | Description | PR |
|---|---|---|---|
| `backend/mvnw`, `backend/.mvn/wrapper/*` | Create | Maven 3.9.6 wrapper | #1 |
| `.github/workflows/ci.yml` | Create | PR/push validation | #2 |
| `backend/pom.xml` | Modify | `ci` profile skips JaCoCo check when `CI=true` | #2 |
| `openspec/config.yaml` | Modify | `coverage_threshold: 0.80` | #2 |
| `README.md` | Modify | CI badge, wrapper commands | #2 |
| `backend/Dockerfile` | Create | Multi-stage non-root image | #3 |
| `.github/workflows/release.yml` | Create | Tag-based GHCR push | #3 |
| `.github/dependabot.yml` | Create | Weekly Maven + GitHub Actions updates | #3 |
| `README.md` | Modify | Docker build/run example | #3 |

## Interfaces / Contracts

### `backend/pom.xml` CI profile

Add `<jacoco.check.skip>false</jacoco.check.skip>`, a profile activated by `<property><name>env.CI</name></property>` that sets it to `true`, and `<skip>${jacoco.check.skip}</skip>` on the `jacoco:check` execution. Local `mvn verify` continues to enforce the 80% line-coverage gate; CI reports coverage without failing on percentage.

> **Risk note:** `pom.xml` currently hardcodes `root`/`password` for the local Flyway Maven plugin. The `verify` lifecycle does not invoke this plugin by default, so CI will not use those credentials. Treat them as local-only and remove/rotate them in a future hardening change.

### `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: backend
    permissions:
      contents: read
      actions: write
    steps:
      - uses: actions/checkout@v4
        with:
          persist-credentials: false

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - uses: docker/setup-buildx-action@v3

      - name: Make mvnw executable
        run: chmod +x mvnw

      - name: Build and verify
        run: ./mvnw -B verify -DskipITs=false spotless:check jacoco:report

      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: jacoco-report
          path: backend/target/site/jacoco/

      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-reports
          path: |
            backend/target/surefire-reports/
            backend/target/failsafe-reports/
```

### `.github/workflows/release.yml`

```yaml
name: Release

on:
  push:
    tags:
      - 'v*.*.*'

jobs:
  release:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4
        with:
          persist-credentials: false

      - uses: docker/setup-buildx-action@v3

      - uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - uses: docker/metadata-action@v5
        id: meta
        with:
          images: ghcr.io/${{ github.repository_owner }}/tacosoft-backend
          tags: |
            type=ref,event=tag

      - uses: docker/build-push-action@v5
        with:
          context: backend
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
```

### `.github/dependabot.yml`

```yaml
version: 2
updates:
  - package-ecosystem: maven
    directory: /backend
    schedule:
      interval: weekly

  - package-ecosystem: github-actions
    directory: /
    schedule:
      interval: weekly
```

### `backend/Dockerfile`

```dockerfile
# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src ./src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER appuser
ENV DB_URL="" \
    DB_USER="" \
    DB_PASSWORD="" \
    JWT_SECRET="" \
    SERVER_PORT=8080 \
    CORS_ORIGINS=""
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> **Risk note:** Base images use mutable tags. Pin to sha256 digests when supply-chain reproducibility becomes a requirement.

## Testing Strategy

| Layer | What to Test | Approach |
|---|---|---|
| Wrapper | Maven 3.9.6 resolution | `cd backend && ./mvnw -version` |
| Local build | 80% gate still enforced | `cd backend && ./mvnw -B verify -DskipITs=false spotless:check jacoco:report` |
| CI behavior | Gate skipped, artifacts uploaded | Run workflow on PR; inspect uploaded reports |
| Container | Non-root, port 8080 | `docker build -t tacosoft-backend backend/` then `docker run --rm tacosoft-backend id` |
| Release | Tag `v1.2.3` → image `v1.2.3` | Push tag and verify GHCR tag |

## Migration / Rollout

No data migration. Deploy in PR order: wrapper → CI/config → Docker/release. Branch protection requiring CI pass on `main` is enabled by a repo admin after PR #2 merges.

## Open Questions

- Repository owner in README badge is currently `evertdaniel`; confirm before merging PR #2.
- Branch protection on `main` must be configured by a repository admin after CI is active.
