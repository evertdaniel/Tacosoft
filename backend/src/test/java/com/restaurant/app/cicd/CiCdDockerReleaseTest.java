package com.restaurant.app.cicd;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Approval-style tests for the Docker image, release workflow, Dependabot config, and README Docker
 * documentation introduced in PR #3.
 *
 * <p>These tests verify the structural contract between the spec, the design, and the actual
 * repository files. They are infrastructure/config tests, not business-logic tests.
 */
class CiCdDockerReleaseTest {

    private static Path projectRoot;

    @BeforeAll
    static void locateProjectRoot() {
        // Surefire runs from the backend module directory, so the repo root is one level up.
        projectRoot = Path.of("..").toAbsolutePath().normalize();
    }

    @Test
    void dockerfileHasMultiStageBuildAndNonRootUser() throws Exception {
        Path dockerfile = projectRoot.resolve("backend/Dockerfile");
        assertThat(dockerfile).exists();

        String content = Files.readString(dockerfile);
        assertThat(content).contains("FROM eclipse-temurin:21-jdk-alpine AS build");
        assertThat(content).contains("FROM eclipse-temurin:21-jre-alpine");
        assertThat(content).contains("./mvnw -B package -DskipTests");
        assertThat(content).contains("adduser -S appuser");
        assertThat(content).contains("USER appuser");
        assertThat(content).contains("EXPOSE 8080");
        assertThat(content).contains("ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]");
        assertThat(content).contains("DB_URL");
        assertThat(content).contains("SERVER_PORT");
        assertThat(content).contains("CORS_ORIGINS");
    }

    @Test
    void releaseWorkflowPublishesImageOnVersionTags() throws Exception {
        Path workflow = projectRoot.resolve(".github/workflows/release.yml");
        assertThat(workflow).exists();

        String content = Files.readString(workflow);
        assertThat(content).contains("name: Release");
        assertThat(content).contains("tags:");
        assertThat(content).contains("'v*.*.*'");
        assertThat(content).contains("docker/setup-buildx-action@v3");
        assertThat(content).contains("docker/login-action@v3");
        assertThat(content).contains("docker/metadata-action@v5");
        assertThat(content).contains("docker/build-push-action@v5");
        assertThat(content).contains("ghcr.io/${{ github.repository_owner }}/tacosoft-backend");
        assertThat(content).contains("type=ref,event=tag");
        assertThat(content).contains("push: true");
        assertThat(content).contains("context: backend");
        assertThat(content).contains("persist-credentials: false");
        assertThat(content).contains("contents: read");
        assertThat(content).contains("packages: write");
    }

    @Test
    void dependabotChecksMavenAndGitHubActionsWeekly() throws Exception {
        Path dependabot = projectRoot.resolve(".github/dependabot.yml");
        assertThat(dependabot).exists();

        String content = Files.readString(dependabot);
        assertThat(content).contains("version: 2");
        assertThat(content).contains("package-ecosystem: maven");
        assertThat(content).contains("directory: /backend");
        assertThat(content).contains("package-ecosystem: github-actions");
        assertThat(content).contains("directory: /");
        assertThat(content).contains("interval: weekly");
    }

    @Test
    void readmeContainsDockerBuildAndRunExamples() throws Exception {
        Path readme = projectRoot.resolve("README.md");
        String content = Files.readString(readme);

        assertThat(content).contains("docker build -t tacosoft-backend backend/");
        assertThat(content).contains("docker run");
    }
}
