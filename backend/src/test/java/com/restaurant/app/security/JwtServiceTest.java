package com.restaurant.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Unit tests for JwtService. SPEC-AUTH-001 acceptance criteria. */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks private JwtService jwtService;

    @Mock private AppUser mockUser;

    @BeforeEach
    void setUp() {
        // Set required properties via reflection
        ReflectionTestUtils.setField(
                jwtService, "secret", "test-secret-key-that-is-long-enough-for-hs256");
        ReflectionTestUtils.setField(jwtService, "expirationMinutes", 120L);
    }

    @Test
    void generateToken_ValidUser_ReturnsJwt() {
        // Arrange
        AppUser user = createTestUser();

        // Act
        String token = jwtService.generateToken(user);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void validateToken_ValidToken_ReturnsClaims() {
        // Arrange
        AppUser user = createTestUser();
        String token = jwtService.generateToken(user);

        // Act
        Claims claims = jwtService.validateToken(token);

        // Assert
        assertThat(claims).isNotNull();
        assertThat(claims.get("sub")).isEqualTo(user.getId().toString());
        assertThat(claims.get("username")).isEqualTo("testuser");
        assertThat(claims.get("role")).isEqualTo("ADMIN");
    }

    @Test
    void validateToken_InvalidToken_ReturnsNull() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        Claims claims = jwtService.validateToken(invalidToken);

        // Assert
        assertThat(claims).isNull();
    }

    @Test
    void validateToken_ExpiredToken_ReturnsNull() {
        // Arrange - Create service with 0 expiration
        JwtService expiredService = new JwtService();
        ReflectionTestUtils.setField(
                expiredService, "secret", "test-secret-key-that-is-long-enough-for-hs256");
        ReflectionTestUtils.setField(expiredService, "expirationMinutes", -1L); // Already expired

        AppUser user = createTestUser();
        String token = expiredService.generateToken(user);

        // Act
        Claims claims = expiredService.validateToken(token);

        // Assert
        assertThat(claims).isNull();
    }

    @Test
    void extractUserId_ValidToken_ReturnsUserId() {
        // Arrange
        AppUser user = createTestUser();
        String token = jwtService.generateToken(user);
        String expectedUserId = user.getId().toString();

        // Act
        String userId = jwtService.extractUserId(token);

        // Assert
        assertThat(userId).isEqualTo(expectedUserId);
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Arrange
        AppUser user = createTestUser();
        String token = jwtService.generateToken(user);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void extractRestaurantRoles_ValidToken_ReturnsRoles() {
        // Arrange
        AppUser user = createTestUser();
        String token = jwtService.generateToken(user);

        // Act
        List<java.util.Map<String, String>> roles = jwtService.extractRestaurantRoles(token);

        // Assert
        assertThat(roles).isNotNull();
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).get("restaurantId")).isEqualTo("restaurant-1");
        assertThat(roles.get(0).get("role")).isEqualTo("ADMIN");
    }

    @Test
    void extractClaims_TamperedToken_ReturnsNull() {
        // Arrange
        AppUser user = createTestUser();
        String token = jwtService.generateToken(user);
        String tamperedToken = token + "tampered";

        // Act
        Claims claims = jwtService.validateToken(tamperedToken);

        // Assert
        assertThat(claims).isNull();
    }

    @Test
    void tokenContainsRequiredClaims() {
        // Arrange
        AppUser user = createTestUser();
        String token = jwtService.generateToken(user);
        Claims claims = jwtService.validateToken(token);

        // Assert - Verify all required claims are present
        assertThat(claims.get("sub")).isNotNull();
        assertThat(claims.get("username")).isNotNull();
        assertThat(claims.get("role")).isNotNull();
        assertThat(claims.get("restaurantRoles")).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getIssuedAt()).isNotNull();
    }

    @Test
    void tokenExpiration_IsFuture() {
        // Arrange
        AppUser user = createTestUser();
        String token = jwtService.generateToken(user);
        Claims claims = jwtService.validateToken(token);

        // Assert
        assertThat(claims.getExpiration()).isAfter(java.time.Instant.now());
    }

    // Helper methods

    private AppUser createTestUser() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("testuser");
        user.setPassword("encoded-password");
        user.setActive(true);

        // Create primary role
        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");
        user.setPrimaryRole(adminRole);

        // Create user restaurant role
        UserRestaurantRole restaurantRole = new UserRestaurantRole();
        restaurantRole.setRestaurantId("restaurant-1");
        restaurantRole.setRole(adminRole);
        restaurantRole.setUser(user);

        user.setRestaurantRoles(List.of(restaurantRole));

        return user;
    }
}
