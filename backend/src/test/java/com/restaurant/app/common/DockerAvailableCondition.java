package com.restaurant.app.common;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/** JUnit 5 condition that enables a test only when Docker is available. */
public class DockerAvailableCondition implements ExecutionCondition {

    private static final String REASON_ENABLED = "Docker is available";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                return ConditionEvaluationResult.enabled(REASON_ENABLED);
            }
        } catch (Throwable t) {
            return ConditionEvaluationResult.disabled(
                    "Docker availability check failed: " + t.getMessage());
        }
        return ConditionEvaluationResult.disabled("Docker is not available");
    }
}
