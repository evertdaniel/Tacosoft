package com.restaurant.app;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Testcontainers configuration for integration tests using MySQL.
 *
 * <p>This configuration provides a real MySQL database for tests that need full MySQL compatibility
 * (Flyway migrations, utf8mb4 charset, etc.).
 *
 * <p>For local testing with installed MySQL, set environment variables: -
 * DB_URL=jdbc:mysql://localhost:3306/restaurant_test - DB_USER=root - DB_PASS=
 */
@TestConfiguration
public class MySQLTestcontainersConfig {

    public static MySQLContainer<?> MYSQL_CONTAINER;

    static {
        // Only start Testcontainers if not using local MySQL
        String dbUrl = System.getProperty("DB_URL", System.getenv("DB_URL"));
        if (dbUrl == null) {
            MYSQL_CONTAINER =
                    new MySQLContainer<>(
                                    org.testcontainers.utility.DockerImageName.parse("mysql:8.0"))
                            .withDatabaseName("restaurant_test")
                            .withUsername("test")
                            .withPassword("test");
            MYSQL_CONTAINER.start();
        }
    }

    @DynamicPropertySource
    public static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        String dbUrl = System.getProperty("DB_URL", System.getenv("DB_URL"));
        String dbUser = System.getProperty("DB_USER", System.getenv("DB_USER"));
        String dbPass = System.getProperty("DB_PASS", System.getenv("DB_PASS"));

        if (dbUrl != null) {
            // Use local MySQL
            registry.add("spring.datasource.url", () -> dbUrl);
            registry.add("spring.datasource.username", () -> dbUser != null ? dbUser : "root");
            registry.add("spring.datasource.password", () -> dbPass != null ? dbPass : "");
            registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        } else if (MYSQL_CONTAINER != null) {
            // Use Testcontainers
            registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
            registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        }
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
    }

    @Bean(destroyMethod = "stop")
    MySQLContainer<?> mysqlContainer() {
        return MYSQL_CONTAINER; // Will be null when using local MySQL
    }
}
