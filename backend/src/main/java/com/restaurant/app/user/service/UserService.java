package com.restaurant.app.user.service;

import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.auth.repository.AppUserRepository;
import com.restaurant.app.auth.repository.RoleRepository;
import com.restaurant.app.auth.repository.UserRestaurantRoleRepository;
import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.security.TenantContext;
import com.restaurant.app.user.dto.AssignRoleRequest;
import com.restaurant.app.user.dto.CreateUserRequest;
import com.restaurant.app.user.dto.UpdateUserRequest;
import com.restaurant.app.user.dto.UserDto;
import com.restaurant.app.user.mapper.UserMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for user management operations. */
@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRestaurantRoleRepository userRestaurantRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            AppUserRepository userRepository,
            RoleRepository roleRepository,
            UserRestaurantRoleRepository userRestaurantRoleRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRestaurantRoleRepository = userRestaurantRoleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /** Get all users for the current restaurant. */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        String restaurantId = TenantContext.getRestaurantId();
        return userRepository.findByRestaurantId(restaurantId).stream()
                .map(userMapper::toDto)
                .toList();
    }

    /** Get a user by ID. */
    @Transactional(readOnly = true)
    public UserDto getUserById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        AppUser user =
                userRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("User", id));
        return userMapper.toDto(user);
    }

    /** Create a new user. */
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        // Check username uniqueness
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }

        // Validate primary role
        com.restaurant.app.auth.model.Role role =
                roleRepository
                        .findById(request.getPrimaryRoleId())
                        .orElseThrow(
                                () -> new NotFoundException("Role", request.getPrimaryRoleId()));

        AppUser user =
                AppUser.builder()
                        .id(UUID.randomUUID().toString())
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .primaryRoleId(request.getPrimaryRoleId())
                        .primaryRole(role)
                        .active(true)
                        .build();

        // Create initial restaurant role
        UserRestaurantRole userRole = new UserRestaurantRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setRestaurantId(restaurantId);
        userRole.setUserId(user.getId());
        userRole.setUser(user);
        userRole.setRoleId(role.getId());
        userRole.setRole(role);

        user.setRestaurantRoles(List.of(userRole));
        user = userRepository.save(user);

        return userMapper.toDto(user);
    }

    /** Update a user. */
    @Transactional
    public UserDto updateUser(String id, UpdateUserRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        AppUser user =
                userRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("User", id));

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getPrimaryRoleId() != null) {
            com.restaurant.app.auth.model.Role role =
                    roleRepository
                            .findById(request.getPrimaryRoleId())
                            .orElseThrow(
                                    () ->
                                            new NotFoundException(
                                                    "Role", request.getPrimaryRoleId()));
            user.setPrimaryRoleId(request.getPrimaryRoleId());
            user.setPrimaryRole(role);
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        if (request.getPersonId() != null) {
            user.setPersonId(request.getPersonId());
        }

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    /** Delete a user. */
    @Transactional
    public void deleteUser(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        AppUser user =
                userRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("User", id));
        userRepository.delete(user);
    }

    /** Assign a restaurant role to a user. */
    @Transactional
    public void assignRestaurantRole(String userId, AssignRoleRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        AppUser user =
                userRepository
                        .findByIdAndRestaurantId(userId, restaurantId)
                        .orElseThrow(() -> new NotFoundException("User", userId));

        com.restaurant.app.auth.model.Role role =
                roleRepository
                        .findById(request.getRoleId())
                        .orElseThrow(() -> new NotFoundException("Role", request.getRoleId()));

        // Check if role already assigned
        boolean alreadyAssigned =
                userRestaurantRoleRepository.existsByUserIdAndRestaurantIdAndRoleId(
                        userId, request.getRestaurantId(), request.getRoleId());

        if (alreadyAssigned) {
            throw new ConflictException("User already has this role in the restaurant");
        }

        UserRestaurantRole userRole = new UserRestaurantRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setRestaurantId(request.getRestaurantId());
        userRole.setUserId(userId);
        userRole.setUser(user);
        userRole.setRoleId(role.getId());
        userRole.setRole(role);

        userRestaurantRoleRepository.save(userRole);
    }

    /** Remove a restaurant role from a user. */
    @Transactional
    public void removeRestaurantRole(String userId, String restaurantId, Integer roleId) {
        String currentRestaurantId = TenantContext.getRestaurantId();
        UserRestaurantRole userRole =
                userRestaurantRoleRepository
                        .findByUserIdAndRestaurantIdAndRoleId(userId, restaurantId, roleId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "User role",
                                                userId + "-" + restaurantId + "-" + roleId));

        userRestaurantRoleRepository.delete(userRole);
    }
}
