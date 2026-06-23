# Tasks: Setup GitHub Actions CI/CD for Tacosoft Backend

## Review Workload Forecast

| Field | Value |
|---|---|
| Estimated changed lines | ~2,800 total; ~200 excl. generated wrapper |
| 400-line budget risk | Low |
| Chained PRs recommended | Yes |
| Suggested split | PR #1 wrapper → PR #2 CI/config → PR #3 Docker/release |
| Delivery strategy | auto-chain |
| Chain strategy | stacked-to-main |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Branch | Base | Notes |
|---|---|---|---|---|
| 1 | Maven wrapper | `feature/setup-ci-cd-wrapper` | `main` | Generated; size-exception |
| 2 | CI workflow + config | `feature/setup-ci-cd-ci` | PR #1 branch until merged | Verify local build |
| 3 | Dockerfile + release + dependabot | `feature/setup-ci-cd-release` | PR #2 branch until merged | Verify Docker build |

## Phase 1: PR #1 — Maven Wrapper (`feature/setup-ci-cd-wrapper`)

- [x] **1.1** (PR#1) Generate Maven 3.9.6 wrapper in `backend/` and set executable bit. Files: `backend/mvnw`, `backend/.mvn/wrapper/*`. AC: `./mvnw -version` reports 3.9.6; `git update-index --chmod=+x backend/mvnw`. Lines: ~2,600 generated (size-exception). Deps: none.

## Phase 2: PR #2 — CI Workflow & Config (`feature/setup-ci-cd-ci`)

- [x] **2.1** (PR#2) Create `.github/workflows/ci.yml`. Files: `.github/workflows/ci.yml`. AC: push/PR to `main`; Temurin 21; runs `./mvnw -B verify -DskipITs=false spotless:check jacoco:report`; uploads reports. Lines: ~55. Deps: 1.1.
- [x] **2.2** (PR#2) Add `ci` profile to `backend/pom.xml`. Files: `backend/pom.xml`. AC: `env.CI` sets `jacoco.check.skip=true`; local build enforces 80%. Lines: ~30. Deps: none.
- [x] **2.3** (PR#2) Set OpenSpec coverage threshold to 0.80. Files: `openspec/config.yaml`. AC: `verify.coverage_threshold: 0.80`. Lines: ~1. Deps: 2.2.
- [x] **2.4** (PR#2) Update README: CI badge and `./mvnw` commands. Files: `README.md`. AC: badge placeholder; examples use `./mvnw`. Lines: ~20. Deps: 1.1.
- [x] **2.5** (PR#2) Verify PR #2 locally. Files: none. AC: `./mvnw -B verify ...` passes; `CI=true` skips coverage gate. Lines: 0. Deps: 2.1–2.4.

## Phase 3: PR #3 — Docker, Release & Dependabot (`feature/setup-ci-cd-release`)

- [x] **3.1** (PR#3) Create `backend/Dockerfile`. Files: `backend/Dockerfile`. AC: multi-stage Temurin 21 build/JRE; non-root; exposes 8080; accepts env vars. Lines: ~25. Deps: 1.1.
- [x] **3.2** (PR#3) Create `.github/workflows/release.yml`. Files: `.github/workflows/release.yml`. AC: triggers on `v*.*.*`; pushes `ghcr.io/{OWNER}/tacosoft-backend:{TAG}` preserving `v`. Lines: ~40. Deps: 3.1.
- [x] **3.3** (PR#3) Create `.github/dependabot.yml`. Files: `.github/dependabot.yml`. AC: weekly Maven (`/backend`) and GitHub Actions (`/`). Lines: ~12. Deps: none.
- [x] **3.4** (PR#3) Add Docker build/run example to README. Files: `README.md`. AC: `docker build` and `docker run` examples present. Lines: ~15. Deps: 3.1.
- [x] **3.5** (PR#3) Verify Docker image and open PR #3. Files: none. AC: `docker build` succeeds; `docker run --rm tacosoft-backend id` shows non-root. Lines: 0. Deps: 3.1–3.4.

## Phase 4: Verification & Rollout

- [ ] **4.1** Confirm CI passes on `main` and release works on test tag. Files: `.github/workflows/ci.yml`, `.github/workflows/release.yml`. AC: green check on `main` with artifacts; `v0.0.0-test` creates GHCR package with same tag.
- [ ] **4.2** Record branch protection recommendation. Files: `README.md` or `design.md`. AC: note requiring `CI` status check on `main`.
