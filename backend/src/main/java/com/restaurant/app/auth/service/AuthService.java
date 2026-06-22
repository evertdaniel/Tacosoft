package com.restaurant.app.auth.service;

import com.restaurant.app.auth.dto.LoginRequest;
import com.restaurant.app.auth.dto.LoginResponse;
import com.restaurant.app.auth.dto.RestaurantInfoDto;
import com.restaurant.app.auth.dto.UserDto;
import com.restaurant.app.auth.mapper.RoleMapper;
import com.restaurant.app.auth.mapper.UserMapper;
import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.auth.repository.AppUserRepository;
import com.restaurant.app.common.UnauthorizedException;
import com.restaurant.app.security.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for authentication operations. */
@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper,
            RoleMapper roleMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    /** Authenticate user and generate JWT token. */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Find user by username
        AppUser user =
                userRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        // Load full user with relationships
        AppUser fullUser =
                userRepository
                        .findByIdWithRoles(user.getId())
                        .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Update last login
        fullUser.setLastLogin(LocalDateTime.now());
        userRepository.save(fullUser);

        // Generate JWT token
        String token = jwtService.generateToken(fullUser);

        // Build user DTO
        UserDto userDto = userMapper.toDto(fullUser);

        // Determine current restaurant (first one in the list or null)
        RestaurantInfoDto currentRestaurant = determineCurrentRestaurant(fullUser);

        return new LoginResponse(token, userDto, currentRestaurant);
    }

    /**
     * Determine the current restaurant for the user. In a real scenario, this might be based on
     * user preference or last login.
     */
    private RestaurantInfoDto determineCurrentRestaurant(AppUser user) {
        List<UserRestaurantRole> roles = user.getRestaurantRoles();
        if (roles == null || roles.isEmpty()) {
            return null;
        }

        // Return the first restaurant the user has access to
        UserRestaurantRole firstRole = roles.get(0);
        return new RestaurantInfoDto(
                firstRole.getRestaurantId(),
                "Restaurant " + firstRole.getRestaurantId(), // Would load actual name
                firstRole.getRole().getName());
    }
}
