# Archive Report: setup-github-ci-cd

## Change Metadata

| Field | Value |
|-------|-------|
| Change name | `setup-github-ci-cd` |
| Archive date | 2026-06-22 |
| Artifact store mode | `openspec` |
| Archive status | Complete |

## Deliverables

This change introduced the GitHub Actions CI/CD pipeline and supporting artifacts for the Tacosoft backend:

| Artifact | Path | Purpose |
|----------|------|---------|
| CI workflow | `.github/workflows/ci.yml` | PR/push validation (compile, test, Spotless, JaCoCo) |
| Release workflow | `.github/workflows/release.yml` | Tag-based GHCR image push |
| Dependabot config | `.github/dependabot.yml` | Weekly Maven + GitHub Actions dependency scans |
| Maven wrapper | `backend/mvnw`, `backend/.mvn/wrapper/*` | Reproducible Maven 3.9.6 builds |
| Dockerfile | `backend/Dockerfile` | Multi-stage, non-root production image |
| Maven `ci` profile | `backend/pom.xml` | Skips JaCoCo coverage gate when `CI=true` |
| README updates | `README.md` | CI badge, wrapper commands, Docker examples |
| OpenSpec config | `openspec/config.yaml` | Coverage threshold aligned to `0.80` |

## Verification Summary

- **Verdict**: `PASS`
- **Tasks complete**: 12 / 12
- **Local build**: ✅ Passed (`./mvnw -B verify -DskipITs=false spotless:check jacoco:report`)
- **CI-mode build**: ✅ Passed (`CI=true ./mvnw ...` skipped JaCoCo gate as designed)
- **Config tests**: ✅ 14 / 14 passed (`CiCdConfigTest`, `CiCdDockerReleaseTest`)
- **CI on `main`**: ✅ Run `28004949385` completed successfully with artifacts uploaded
- **Release workflow**: ✅ Run `28005085897` pushed `ghcr.io/evertdaniel/tacosoft-backend:v0.0.0-test`
- **Spec compliance**: 15 / 15 scenarios compliant
- **TDD compliance**: 6 / 6 checks passed

## Spec Sync

The delta spec did not follow the `specs/{domain}/spec.md` subfolder convention, so it was treated as a full specification for the new `github-ci-cd` domain.

- **Action**: Created main spec `openspec/specs/github-ci-cd/spec.md` from `openspec/changes/setup-github-ci-cd/spec.md`.
- **Requirements added**: 7 (CI Workflow, Maven Wrapper, Production Dockerfile, Release Workflow, Dependabot Configuration, README Updates, OpenSpec Coverage Threshold)
- **Existing main spec**: None — new domain.

## Traceability

Engram observations referenced during archive:

| Artifact | Observation ID | Topic |
|----------|----------------|-------|
| Proposal | `#39` | `sdd/setup-github-ci-cd/proposal` |
| Spec | `#40` | `sdd/setup-github-ci-cd/spec` |
| Design | `#41` | `sdd/setup-github-ci-cd/design` |
| Tasks | `#42` | `sdd/setup-github-ci-cd/tasks` |
| Apply progress | `#43` | `sdd/setup-github-ci-cd/apply-progress` |
| Verify report | `#47` | `sdd/setup-github-ci-cd/verify-report` |

## Residual Risks

| Risk | Likelihood | Impact | Mitigation / Note |
|------|------------|--------|-------------------|
| Mutable Temurin 21 Alpine base images may drift | Medium | Low | Already documented in `design.md`; pin to sha256 digests if supply-chain reproducibility becomes a requirement. |
| Branch protection requiring CI status check on `main` is not enforced by code | N/A | Medium | Recorded in `README.md`; repository admin must enable the `CI` status check requirement in GitHub settings. |
| Local Docker legacy builder deprecation warning | Low | Low | CI uses `docker/setup-buildx-action@v3`; no functional impact. |

## Notes

- No destructive deltas were merged; all prior main specs remain unchanged.
- `openspec/config.yaml` already contains `verify.coverage_threshold: 0.80`; no additional config update was required.
- The archived change folder is retained as an audit trail and should not be modified.
