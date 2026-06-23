package com.restaurant.app.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Skips the annotated test or test class when Docker is not available.
 *
 * <p>This is used for Testcontainers/MySQL-dependent tests so that they are explicitly skipped (not
 * silently excluded) when the environment cannot run containers.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DockerAvailableCondition.class)
public @interface EnabledIfDockerAvailable {
    /** Reason reported when Docker is unavailable. */
    String reason() default "Docker is not available";
}
