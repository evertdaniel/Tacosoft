package com.restaurant.app.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.restaurant.app.auth.dto.LoginRequest;
import com.restaurant.app.auth.dto.LoginResponse;
import com.restaurant.app.auth.dto.RestaurantRoleDto;
import com.restaurant.app.auth.dto.RoleDto;
import com.restaurant.app.auth.dto.UserDto;
import com.restaurant.app.auth.mapper.RoleMapper;
import com.restaurant.app.auth.mapper.UserMapper;
import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.auth.repository.AppUserRepository;
import com.restaurant.app.common.UnauthorizedException;
import com.restaurant.app.security.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Unit tests for AuthService. SPEC-AUTH-001 acceptance criteria. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AppUserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private JwtService jwtService;

    @Mock private UserMapper userMapper;

    @Mock private RoleMapper roleMapper;

    @InjectMocks private AuthService authService;

    private AppUser testUser;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = createTestUser();

        // Create valid login request
        validLoginRequest = new LoginRequest("testuser", "raw-password");

        // Stub mapper to avoid null DTOs
        lenient()
                .when(userMapper.toDto(any(AppUser.class)))
                .thenReturn(
                        new UserDto(
                                testUser.getId(),
                                testUser.getUsername(),
                                "Test",
                                "User",
                                "test@example.com",
                                true,
                                new RoleDto(1, "ADMIN", "Administrator"),
                                List.of(
                                        new RestaurantRoleDto(
                                                "restaurant-1",
                                                "Restaurant restaurant-1",
                                                new RoleDto(1, "ADMIN", "Administrator")))));
    }

    @Test
    void login_ValidCredentials_ReturnsLoginResponse() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(userRepository.findByIdWithRoles(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authService.login(validLoginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user()).isNotNull();

        // Verify last login was updated
        assertThat(testUser.getLastLogin()).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    void login_InvalidUsername_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        LoginRequest request = new LoginRequest("nonexistent", "password");
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_InvalidPassword_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        // Act & Assert
        LoginRequest request = new LoginRequest("testuser", "wrong-password");
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_InactiveUser_ThrowsUnauthorizedException() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User account is inactive");
    }

    @Test
    void login_UserNotFoundAfterAuth_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(userRepository.findByIdWithRoles(testUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void login_GeneratesJwtToken() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(userRepository.findByIdWithRoles(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("generated-jwt-token");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authService.login(validLoginRequest);

        // Assert
        assertThat(response.token()).isEqualTo("generated-jwt-token");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void login_UpdatesLastLoginTimestamp() {
        // Arrange
        LocalDateTime beforeLogin = LocalDateTime.now();
        testUser.setLastLogin(null);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(userRepository.findByIdWithRoles(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(
                        invocation -> {
                            AppUser user = invocation.getArgument(0);
                            assertThat(user.getLastLogin()).isNotNull();
                            assertThat(user.getLastLogin()).isAfter(beforeLogin);
                            return user;
                        });

        // Act
        authService.login(validLoginRequest);

        // Assert
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void login_ReturnsCurrentRestaurantInfo() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(userRepository.findByIdWithRoles(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authService.login(validLoginRequest);

        // Assert
        assertThat(response.currentRestaurant()).isNotNull();
        assertThat(response.currentRestaurant().id()).isEqualTo("restaurant-1");
        assertThat(response.currentRestaurant().role()).isEqualTo("ADMIN");
    }

    @Test
    void login_UserWithNoRestaurantRoles_ReturnsNullCurrentRestaurant() {
        // Arrange
        testUser.setRestaurantRoles(List.of());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(userRepository.findByIdWithRoles(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authService.login(validLoginRequest);

        // Assert
        assertThat(response.currentRestaurant()).isNull();
    }

    // Helper methods

    private AppUser createTestUser() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("testuser");
        user.setPassword("encoded-password");
        user.setActive(true);
        user.setLastLogin(null);

        // Create role
        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");

        // Create user restaurant role
        UserRestaurantRole restaurantRole = new UserRestaurantRole();
        restaurantRole.setRestaurantId("restaurant-1");
        restaurantRole.setRole(adminRole);
        restaurantRole.setUser(user);

        user.setRestaurantRoles(List.of(restaurantRole));

        return user;
    }
}
