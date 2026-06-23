package com.restaurant.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.Role;
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
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Unit tests for {@link UserService}. */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private AppUserRepository userRepository;

    @Mock private RoleRepository roleRepository;

    @Mock private UserRestaurantRoleRepository userRestaurantRoleRepository;

    @Mock private UserMapper userMapper;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private final String restaurantId = "restaurant-1";
    private final String userId = "user-1";
    private final Integer roleId = 1;

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAllUsers_ReturnsUsersForRestaurant() {
        AppUser user = userEntity(userId, "john");
        when(userRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto(userId));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(userId);
        verify(userRepository).findByRestaurantId(restaurantId);
    }

    @Test
    void getUserById_ExistingUser_ReturnsDto() {
        AppUser user = userEntity(userId, "john");
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto(userId));

        UserDto result = userService.getUserById(userId);

        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void getUserById_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void createUser_ValidRequest_SavesAndReturnsDto() {
        CreateUserRequest request = createRequest();
        Role role = roleEntity(roleId, "ADMIN");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toDto(any(AppUser.class))).thenReturn(userDto(userId));

        UserDto result = userService.createUser(request);

        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository)
                .save(
                        argThat(
                                u ->
                                        u.getUsername().equals("john")
                                                && u.getPassword().equals("encoded")
                                                && u.getRestaurantRoles().size() == 1
                                                && u.getRestaurantRoles()
                                                        .get(0)
                                                        .getRestaurantId()
                                                        .equals(restaurantId)
                                                && u.getRestaurantRoles().get(0).getRoleId()
                                                        == roleId));
    }

    @Test
    void createUser_DuplicateUsername_ThrowsConflictException() {
        CreateUserRequest request = createRequest();
        when(userRepository.findByUsername(request.getUsername()))
                .thenReturn(Optional.of(userEntity(userId, request.getUsername())));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void createUser_RoleNotFound_ThrowsNotFoundException() {
        CreateUserRequest request = createRequest();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Role");
    }

    @Test
    void updateUser_ExistingUser_UpdatesAllFields() {
        AppUser user = userEntity(userId, "john");
        UpdateUserRequest request = updateRequest();
        Role role = roleEntity(roleId, "ADMIN");
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toDto(user)).thenReturn(userDto(userId));

        UserDto result = userService.updateUser(userId, request);

        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(user.getPrimaryRoleId()).isEqualTo(roleId);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getPersonId()).isEqualTo("person-1");
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void updateUser_PartialFields_UpdatesOnlyProvidedFields() {
        AppUser user = userEntity(userId, "john");
        UpdateUserRequest request = new UpdateUserRequest();
        request.setActive(false);
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toDto(user)).thenReturn(userDto(userId));

        userService.updateUser(userId, request);

        assertThat(user.isActive()).isFalse();
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void updateUser_UserNotFound_ThrowsNotFoundException() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setActive(false);
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void updateUser_RoleNotFound_ThrowsNotFoundException() {
        AppUser user = userEntity(userId, "john");
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPrimaryRoleId(roleId);
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Role");
    }

    @Test
    void deleteUser_ExistingUser_DeletesSuccessfully() {
        AppUser user = userEntity(userId, "john");
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void assignRestaurantRole_ValidRequest_SavesRole() {
        AppUser user = userEntity(userId, "john");
        Role role = roleEntity(roleId, "ADMIN");
        AssignRoleRequest request = assignRequest();
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRestaurantRoleRepository.existsByUserIdAndRestaurantIdAndRoleId(
                        userId, request.getRestaurantId(), roleId))
                .thenReturn(false);

        userService.assignRestaurantRole(userId, request);

        verify(userRestaurantRoleRepository).save(any(UserRestaurantRole.class));
    }

    @Test
    void assignRestaurantRole_AlreadyAssigned_ThrowsConflictException() {
        AppUser user = userEntity(userId, "john");
        Role role = roleEntity(roleId, "ADMIN");
        AssignRoleRequest request = assignRequest();
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRestaurantRoleRepository.existsByUserIdAndRestaurantIdAndRoleId(
                        userId, request.getRestaurantId(), roleId))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.assignRestaurantRole(userId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("User already has this role");
    }

    @Test
    void assignRestaurantRole_UserNotFound_ThrowsNotFoundException() {
        AssignRoleRequest request = assignRequest();
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRestaurantRole(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void assignRestaurantRole_RoleNotFound_ThrowsNotFoundException() {
        AppUser user = userEntity(userId, "john");
        AssignRoleRequest request = assignRequest();
        when(userRepository.findByIdAndRestaurantId(userId, restaurantId))
                .thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRestaurantRole(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Role");
    }

    @Test
    void removeRestaurantRole_ExistingRole_DeletesSuccessfully() {
        UserRestaurantRole userRole = new UserRestaurantRole();
        when(userRestaurantRoleRepository.findByUserIdAndRestaurantIdAndRoleId(
                        userId, restaurantId, roleId))
                .thenReturn(Optional.of(userRole));

        userService.removeRestaurantRole(userId, restaurantId, roleId);

        verify(userRestaurantRoleRepository).delete(userRole);
    }

    @Test
    void removeRestaurantRole_RoleNotFound_ThrowsNotFoundException() {
        when(userRestaurantRoleRepository.findByUserIdAndRestaurantIdAndRoleId(
                        userId, restaurantId, roleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeRestaurantRole(userId, restaurantId, roleId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User role");
    }

    private CreateUserRequest createRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john");
        request.setPassword("secret123");
        request.setPrimaryRoleId(roleId);
        return request;
    }

    private UpdateUserRequest updateRequest() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("newsecret");
        request.setPrimaryRoleId(roleId);
        request.setActive(false);
        request.setPersonId("person-1");
        return request;
    }

    private AssignRoleRequest assignRequest() {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setRestaurantId(restaurantId);
        request.setRoleId(roleId);
        return request;
    }

    private AppUser userEntity(String id, String username) {
        return AppUser.builder()
                .id(id)
                .username(username)
                .password("password")
                .active(true)
                .primaryRoleId(roleId)
                .primaryRole(roleEntity(roleId, "ADMIN"))
                .restaurantRoles(List.of())
                .build();
    }

    private Role roleEntity(Integer id, String name) {
        return Role.builder().id(id).name(name).description(name + " role").build();
    }

    private UserDto userDto(String id) {
        return new UserDto(id, "john", true, null, List.of(), null);
    }
}
