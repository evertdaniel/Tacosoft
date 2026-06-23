# Proposal: Setup GitHub Actions CI/CD for Tacosoft Backend

## Intent

The repository has no CI/CD automation. PRs and pushes to `main` rely on local validation, allowing formatting drift, test regressions, and coverage drops. This change adds GitHub Actions to compile, test, format, and package the backend automatically.

## Scope

### In Scope
- `ci.yml`: Java 21 compile, unit + Docker integration tests, Spotless, JaCoCo report, Maven caching.
- Maven wrapper (`backend/mvnw` + `.mvn/wrapper/`) for reproducible CI and README alignment.
- Multi-stage `backend/Dockerfile` for containerized releases.
- `release.yml`: push image to GHCR on `v*.*.*` tags.
- `dependabot.yml` for Maven and GitHub Actions.
- Recommend branch protection requiring CI pass on `main`.

### Out of Scope
- Deployment (no target specified).
- CodeQL or semantic-release automation.

## Capabilities

### New Capabilities
- `github-ci-cd`: GitHub Actions CI/CD pipeline for backend PR validation, image build, and tag-based GHCR release.

### Modified Capabilities
- None

## Approach

Run `./mvnw -B verify -DskipITs=false spotless:check jacoco:report` on `ubuntu-latest` with Eclipse Temurin 21 and Docker enabled. Cache `~/.m2` and upload JaCoCo/test reports. Report coverage without enforcing the 80% gate until baseline confirmed.

The Dockerfile uses a multi-stage build (Temurin 21 build + JRE runtime). The release workflow triggers on `v*.*.*` tags, logs in to GHCR with `GITHUB_TOKEN`, and pushes the image.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `.github/workflows/ci.yml` | New | PR/push validation |
| `.github/workflows/release.yml` | New | Tag-based GHCR push |
| `.github/dependabot.yml` | New | Dependency updates |
| `backend/Dockerfile` | New | Production image |
| `backend/mvnw` + `.mvn/wrapper/` | New | Maven wrapper |
| `README.md` | Modified | CI badge and commands |
| `openspec/config.yaml` | Modified | Align coverage threshold |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Maven wrapper bloats PR diff | High | Treat wrapper as generated code |
| First slice exceeds 400-line budget | Medium | Chain wrapper/Dockerfile if needed |
| Coverage gate mismatch blocks CI | Med | Report only until baseline confirmed |
| Self-hosted runners lack Docker | Low | Tests skip via `@EnabledIfDockerAvailable` |

## Rollback Plan

Delete or disable the workflows, revert Dockerfile/wrapper/README changes, and disable Dependabot. CI failures do not mutate production data.

## Dependencies

- GitHub Actions enabled.
- `GITHUB_TOKEN` with `packages:write` for GHCR.
- Repository admin to configure branch protection on `main`.

## Success Criteria

- [ ] CI passes on this PR and subsequent pushes/PRs to `main`.
- [ ] JaCoCo and test reports upload as artifacts.
- [ ] Release workflow pushes an image to GHCR on a `v*.*.*` tag.
- [ ] README documents the wrapper and CI badge.
