package com.restaurant.app.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.List;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * PR #1: Verify all Flyway migrations are portable to H2 (MySQL mode) so integration tests can run
 * without Docker.
 */
@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationPortabilityTest {

    @Autowired private Flyway flyway;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void allMigrationsRunOnH2() {
        assertThatNoException().isThrownBy(() -> flyway.migrate());
    }

    @Test
    void reportingViewsAreQueryableOnH2() {
        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList("SELECT * FROM v_footfall LIMIT 1");
        assertThat(rows).isNotNull();
    }
}
