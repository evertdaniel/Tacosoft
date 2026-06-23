package com.restaurant.app.cicd;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Approval-style tests for the repository-level CI/CD configuration files introduced in PR #2.
 *
 * <p>These tests guard the structural contract between the spec, the design, and the actual files:
 * CI workflow triggers, Maven profile activation, OpenSpec coverage threshold, and README
 * documentation. They are intentionally not business-logic tests because the change itself is
 * infrastructure/configuration.
 */
class CiCdConfigTest {

    private static Path projectRoot;

    @BeforeAll
    static void locateProjectRoot() {
        // Surefire runs from the backend module directory, so the repo root is one level up.
        projectRoot = Path.of("..").toAbsolutePath().normalize();
    }

    @Test
    void ciWorkflowExistsWithExpectedNameAndTriggers() throws Exception {
        Path workflow = projectRoot.resolve(".github/workflows/ci.yml");
        assertThat(workflow).exists();

        String content = Files.readString(workflow);
        assertThat(content).contains("name: CI");
        assertThat(content).contains("branches: [main]");
        assertThat(content).contains("pull_request:");
        assertThat(content).contains("push:");
    }

    @Test
    void ciWorkflowUsesWrapperBuildCommand() throws Exception {
        Path workflow = projectRoot.resolve(".github/workflows/ci.yml");
        String content = Files.readString(workflow);

        assertThat(content)
                .contains("./mvnw -B verify -DskipITs=false spotless:check jacoco:report");
    }

    @Test
    void ciWorkflowUsesTemurin21AndMavenCache() throws Exception {
        Path workflow = projectRoot.resolve(".github/workflows/ci.yml");
        String content = Files.readString(workflow);

        assertThat(content).contains("actions/setup-java@v4");
        assertThat(content).contains("distribution: temurin");
        assertThat(content).contains("java-version: '21'");
        assertThat(content).contains("cache: maven");
    }

    @Test
    void ciWorkflowUploadsReports() throws Exception {
        Path workflow = projectRoot.resolve(".github/workflows/ci.yml");
        String content = Files.readString(workflow);

        assertThat(content).contains("actions/upload-artifact@v4");
        assertThat(content).contains("name: jacoco-report");
        assertThat(content).contains("name: test-reports");
        assertThat(content).contains("backend/target/site/jacoco/");
        assertThat(content).contains("backend/target/surefire-reports/");
    }

    @Test
    void pomXmlDefinesJacocoCheckSkipProperty() throws Exception {
        Path pom = projectRoot.resolve("backend/pom.xml");
        String content = Files.readString(pom);

        assertThat(content).contains("<jacoco.check.skip>false</jacoco.check.skip>");
    }

    @Test
    void pomXmlHasCiProfileActivatedByEnvCi() throws Exception {
        Path pom = projectRoot.resolve("backend/pom.xml");
        String content = Files.readString(pom);

        assertThat(content).contains("<id>ci</id>");
        assertThat(content).contains("<name>env.CI</name>");
        assertThat(content).contains("<jacoco.check.skip>true</jacoco.check.skip>");
    }

    @Test
    void pomXmlJacocoCheckExecutionSkipsViaProperty() throws Exception {
        Path pom = projectRoot.resolve("backend/pom.xml");
        String content = Files.readString(pom);

        assertThat(content).contains("<skip>${jacoco.check.skip}</skip>");
    }

    @Test
    void openSpecCoverageThresholdIsEightyPercent() throws Exception {
        Path config = projectRoot.resolve("openspec/config.yaml");
        assertThat(config).exists();

        String content = Files.readString(config);
        assertThat(content).contains("coverage_threshold: 0.80");
    }

    @Test
    void readmeContainsCiBadge() throws Exception {
        Path readme = projectRoot.resolve("README.md");
        String content = Files.readString(readme);

        assertThat(content).contains("actions/workflows/ci.yml/badge.svg");
    }

    @Test
    void readmeUsesWrapperCommands() throws Exception {
        Path readme = projectRoot.resolve("README.md");
        String content = Files.readString(readme);

        assertThat(content).contains("./mvnw -version");
        assertThat(content).contains("./mvnw clean compile");
    }
}
