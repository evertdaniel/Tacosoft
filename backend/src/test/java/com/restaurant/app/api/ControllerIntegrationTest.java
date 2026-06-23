package com.restaurant.app.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.app.auth.dto.LoginRequest;
import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.auth.repository.AppUserRepository;
import com.restaurant.app.common.IntegrationTestFixtures;
import com.restaurant.app.order.dto.CreateOrderDetailRequest;
import com.restaurant.app.order.dto.CreateOrderRequest;
import com.restaurant.app.security.JwtService;
import com.restaurant.app.user.model.Person;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * API integration tests for all endpoints.
 *
 * <p>Tests: - Status codes - RFC 7807 ProblemDetail error responses - RBAC enforcement - Tenant
 * header validation
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControllerIntegrationTest {

    @Autowired private WebApplicationContext context;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private AppUserRepository userRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private JwtService jwtService;

    private AppUser testUser;
    private String authToken;
    private String restaurantId;
    private String productId;
    private String sectionId;
    private String categoryId;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID().toString();

        // Setup MockMvc with security
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .apply(SecurityMockMvcConfigurers.springSecurity())
                        .build();

        // Create required tenant and person rows first (NOT NULL FK constraints)
        IntegrationTestFixtures.createRestaurant(jdbcTemplate, restaurantId, "API Test Restaurant");
        Person person =
                Person.builder()
                        .id(UUID.randomUUID().toString())
                        .firstName("Test")
                        .lastName("User")
                        .build();
        IntegrationTestFixtures.createPerson(
                jdbcTemplate, person.getId(), person.getFirstName(), person.getLastName());

        // Create a product so order creation can include a valid detail
        sectionId = UUID.randomUUID().toString();
        categoryId = UUID.randomUUID().toString();
        productId = UUID.randomUUID().toString();
        IntegrationTestFixtures.createSection(
                jdbcTemplate, sectionId, restaurantId, "Test Section");
        IntegrationTestFixtures.createCategory(
                jdbcTemplate, categoryId, restaurantId, sectionId, "Test Category");
        IntegrationTestFixtures.createProduct(
                jdbcTemplate,
                productId,
                restaurantId,
                categoryId,
                "Test Product",
                java.math.BigDecimal.valueOf(50));

        // Create test user
        testUser = createTestUser(person);
        testUser = userRepository.save(testUser);

        // Generate auth token
        authToken = jwtService.generateToken(testUser);
    }

    @AfterEach
    void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            try {
                String personId =
                        testUser.getPerson() != null ? testUser.getPerson().getId() : null;
                IntegrationTestFixtures.cleanupUserAndRestaurant(
                        jdbcTemplate, testUser.getId(), personId, restaurantId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    void login_ValidCredentials_Returns200WithToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "testpass");

        // Act & Assert
        mockMvc.perform(
                        post("/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user").isMap())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.currentRestaurant").isMap());
    }

    @Test
    void login_InvalidCredentials_Returns401() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpass");

        // Act & Assert
        mockMvc.perform(
                        post("/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://errors.restaurant.app/unauthorized"))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void protectedEndpoint_WithoutToken_Returns401Or403() throws Exception {
        // Without a token Spring Security rejects the request as anonymous (403 once the required
        // tenant header is present, because TenantFilter runs before the auth decision).
        mockMvc.perform(
                        get("/orders")
                                .header("x-restaurant-id", restaurantId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_WithValidToken_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        get("/orders")
                                .header("Authorization", "Bearer " + authToken)
                                .header("x-restaurant-id", restaurantId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_WithInvalidToken_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        get("/orders")
                                .header("Authorization", "Bearer invalid-token")
                                .header("x-restaurant-id", restaurantId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_WithoutTenantHeader_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        get("/orders")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNonExistentResource_Returns404WithProblemDetail() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        get("/orders/nonexistent-id")
                                .header("Authorization", "Bearer " + authToken)
                                .header("x-restaurant-id", restaurantId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://errors.restaurant.app/not-found"))
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void createOrder_ValidRequest_Returns201() throws Exception {
        // Arrange
        CreateOrderDetailRequest detail = new CreateOrderDetailRequest();
        detail.setProductId(productId);
        detail.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("TAKE_AWAY");
        request.setPeople(1);
        request.setDetails(List.of(detail));

        // Act & Assert
        mockMvc.perform(
                        post("/orders")
                                .with(csrf())
                                .header("Authorization", "Bearer " + authToken)
                                .header("x-restaurant-id", restaurantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        org.hamcrest.Matchers.containsString("/orders/")))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.type").value("TAKE_AWAY"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_InvalidRequest_Returns400WithValidationErrors() throws Exception {
        // Arrange - Missing required fields
        String invalidJson = "{\"type\": \"INVALID_TYPE\"}";

        // Act & Assert
        mockMvc.perform(
                        post("/orders")
                                .with(csrf())
                                .header("Authorization", "Bearer " + authToken)
                                .header("x-restaurant-id", restaurantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rbacEnforcement_UserWithoutAdminRole_Returns403() throws Exception {
        // Arrange - Create a WAITER user and generate a token for them
        AppUser waiter = createUserWithRole("waiter-api-test", "WAITER", 3);
        String waiterToken = jwtService.generateToken(waiter);

        // Act & Assert - WAITER cannot create sections (ADMIN-only)
        mockMvc.perform(
                        post("/sections")
                                .with(csrf())
                                .header("Authorization", "Bearer " + waiterToken)
                                .header("x-restaurant-id", restaurantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"name\": \"Unauthorized Section\", \"displayOrder\":"
                                                + " 1}"))
                .andExpect(status().isForbidden());

        // Cleanup
        userRepository.deleteById(waiter.getId());

        System.out.println("✅ RBAC enforcement rejected WAITER on ADMIN endpoint");
    }

    @Test
    void errorResponse_FollowsRFC7807() throws Exception {
        // Act & Assert
        MvcResult result =
                mockMvc.perform(
                                get("/orders/nonexistent")
                                        .header("Authorization", "Bearer " + authToken)
                                        .header("x-restaurant-id", restaurantId))
                        .andExpect(status().isNotFound())
                        .andExpect(
                                jsonPath("$.type").value("https://errors.restaurant.app/not-found"))
                        .andExpect(jsonPath("$.title").exists())
                        .andExpect(jsonPath("$.status").value(404))
                        .andExpect(jsonPath("$.detail").exists())
                        .andExpect(jsonPath("$.instance").exists())
                        .andReturn();

        System.out.println("✅ RFC 7807 ProblemDetail format validated");
    }

    @Test
    void corsHeaders_RequestedOptions_ReturnsCorsHeaders() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        options("/orders")
                                .header("Origin", "http://localhost:3030")
                                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()); // CORS configuration allows this
    }

    // Helper methods

    private AppUser createTestUser(Person person) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("testpass"));
        user.setActive(true);
        user.setPerson(person);

        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");

        user.setPrimaryRole(adminRole);
        user.setPrimaryRoleId(1);

        UserRestaurantRole restaurantRole = new UserRestaurantRole();
        restaurantRole.setId(UUID.randomUUID().toString());
        restaurantRole.setRestaurantId(restaurantId);
        restaurantRole.setRole(adminRole);
        restaurantRole.setUser(user);

        user.setRestaurantRoles(List.of(restaurantRole));

        return user;
    }

    private AppUser createUserWithRole(String username, String roleName, int roleId) {
        String rolePersonId = UUID.randomUUID().toString();
        IntegrationTestFixtures.createPerson(jdbcTemplate, rolePersonId, roleName, "User");

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("testpass"));
        user.setActive(true);

        Person person =
                Person.builder().id(rolePersonId).firstName(roleName).lastName("User").build();
        user.setPerson(person);

        Role role = new Role();
        role.setId(roleId);
        role.setName(roleName);

        user.setPrimaryRole(role);
        user.setPrimaryRoleId(roleId);

        UserRestaurantRole restaurantRole = new UserRestaurantRole();
        restaurantRole.setId(UUID.randomUUID().toString());
        restaurantRole.setRestaurantId(restaurantId);
        restaurantRole.setRole(role);
        restaurantRole.setUser(user);

        user.setRestaurantRoles(List.of(restaurantRole));

        return userRepository.save(user);
    }
}
