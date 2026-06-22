package com.restaurant.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.auth.repository.AppUserRepository;
import com.restaurant.app.billing.dto.CreateInvoiceRequest;
import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.service.InvoiceService;
import com.restaurant.app.order.dto.CreateOrderDetailRequest;
import com.restaurant.app.order.dto.CreateOrderRequest;
import com.restaurant.app.order.dto.OrderDto;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.repository.OrderRepository;
import com.restaurant.app.order.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tenant isolation invariant test for INV-06.
 *
 * <p>Validates that user from restaurant A cannot read/write restaurant B data. Multi-tenant
 * isolation is enforced by TenantContext and tenant-scoped queries.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class TenantIsolationTest {

    @Autowired private OrderService orderService;

    @Autowired private InvoiceService invoiceService;

    @Autowired private OrderRepository orderRepository;

    @Autowired private AppUserRepository userRepository;

    @Autowired private JwtService jwtService;

    private final String restaurantA = "restaurant-tenant-a";
    private final String restaurantB = "restaurant-tenant-b";
    private Order orderA;

    @BeforeEach
    void setUp() {
        // Create order in restaurant A
        TenantContext.setRestaurantId(restaurantA);

        CreateOrderDetailRequest detail = new CreateOrderDetailRequest();
        detail.setProductId("product-test");
        detail.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("TAKE_AWAY");
        request.setPeople(1);
        request.setDetails(List.of(detail));

        try {
            orderA =
                    Order.builder()
                            .id("order-tenant-a")
                            .restaurantId(restaurantA)
                            .num(8003)
                            .type("TAKE_AWAY")
                            .status("PENDING")
                            .total(BigDecimal.valueOf(100))
                            .build();
            orderA = orderRepository.save(orderA);
        } finally {
            TenantContext.clear();
        }
    }

    @AfterEach
    void tearDown() {
        try {
            TenantContext.setRestaurantId(restaurantA);
            if (orderA != null && orderA.getId() != null) {
                orderRepository.deleteById(orderA.getId());
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        } finally {
            TenantContext.clear();
        }
    }

    /** INV-06: User from restaurant A cannot read restaurant B orders. */
    @Test
    void orderQuery_RestaurantBUser_CannotReadRestaurantAOrders() {
        // Arrange - User from restaurant B tries to read restaurant A's order
        TenantContext.setRestaurantId(restaurantB);

        // Act - Try to fetch restaurant A's order
        var thrown = assertThatThrownBy(() -> orderService.getOrderById("order-tenant-a"));

        // Assert - Should throw NotFoundException (order not found in restaurant B)
        thrown.isInstanceOf(com.restaurant.app.common.NotFoundException.class);

        // Verify restaurant B user can only see their own orders
        List<OrderDto> orders = orderService.getAllOrders();
        assertThat(orders).isEmpty(); // No orders in restaurant B

        System.out.println("✅ INV-06 PASSED: Restaurant B user cannot read restaurant A orders");
    }

    /** INV-06: User from restaurant A cannot write to restaurant B. */
    @Test
    void orderCreate_RestaurantAUser_CannotWriteToRestaurantB() {
        // Arrange - User from restaurant A tries to create order (should go to restaurant A)
        TenantContext.setRestaurantId(restaurantA);

        CreateOrderDetailRequest detail = new CreateOrderDetailRequest();
        detail.setProductId("product-test");
        detail.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("TAKE_AWAY");
        request.setPeople(1);
        request.setDetails(List.of(detail));

        // Act - Create order (automatically scoped to restaurant A via TenantContext)
        OrderDto createdOrder = orderService.createOrder(request);

        // Assert - Order should belong to restaurant A, not B
        // Note: OrderDto doesn't have restaurantId field, it's implicit from TenantContext

        // Verify it doesn't appear in restaurant B
        TenantContext.setRestaurantId(restaurantB);
        List<OrderDto> restaurantBOrders = orderService.getAllOrders();
        assertThat(restaurantBOrders)
                .noneMatch(order -> order.getId().equals(createdOrder.getId()));

        System.out.println("✅ INV-06 PASSED: Orders are automatically scoped to tenant context");
    }

    /** INV-06: Tenant isolation works for invoices too. */
    @Test
    void invoiceQuery_RestaurantBUser_CannotReadRestaurantAInvoices() {
        // Arrange - Create invoice in restaurant A
        TenantContext.setRestaurantId(restaurantA);

        CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest();
        invoiceRequest.setOrderId(orderA.getId());
        InvoiceDto invoiceA = invoiceService.createInvoice(invoiceRequest);

        // Act - Switch to restaurant B and try to read invoice A
        TenantContext.setRestaurantId(restaurantB);

        // Assert - Should throw NotFoundException
        assertThatThrownBy(() -> invoiceService.getInvoice(invoiceA.getId()))
                .isInstanceOf(com.restaurant.app.common.NotFoundException.class);

        // Verify restaurant B invoices list is empty
        List<InvoiceDto> restaurantBInvoices = invoiceService.listInvoices();
        assertThat(restaurantBInvoices).isEmpty();

        System.out.println("✅ INV-06 PASSED: Invoice queries are tenant-isolated");
    }

    /** INV-06: Tenant context must be set for all operations. */
    @Test
    void operationsWithoutTenantContext_ThrowException() {
        // Arrange - Clear tenant context
        TenantContext.clear();

        // Act & Assert - Operations should fail
        assertThatThrownBy(() -> orderService.getAllOrders())
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> invoiceService.listInvoices())
                .isInstanceOf(IllegalStateException.class);

        System.out.println("✅ INV-06 PASSED: Operations fail without tenant context");
    }

    /** INV-06: JWT tokens contain restaurant roles for multi-tenant access. */
    @Test
    void jwtToken_ContainsRestaurantRoles() {
        // Arrange - Create user with access to multiple restaurants
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("multitenant-user");
        user.setPassword("encoded-password");
        user.setActive(true);

        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");

        UserRestaurantRole roleA = new UserRestaurantRole();
        roleA.setRestaurantId(restaurantA);
        roleA.setRole(adminRole);
        roleA.setUser(user);

        user.setRestaurantRoles(List.of(roleA));

        // Fix UUID to String conversion
        user.setId(UUID.randomUUID().toString());

        UserRestaurantRole roleB = new UserRestaurantRole();
        roleB.setRestaurantId(restaurantB);
        roleB.setRole(adminRole);
        roleB.setUser(user);

        user.setRestaurantRoles(List.of(roleA, roleB));

        // Act - Generate JWT
        String token = jwtService.generateToken(user);

        // Assert - Token should contain both restaurant roles
        List<java.util.Map<String, String>> restaurantRoles =
                jwtService.extractRestaurantRoles(token);
        assertThat(restaurantRoles).hasSize(2);

        java.util.Optional<String> roleARestaurantId =
                restaurantRoles.stream()
                        .filter(r -> r.get("restaurantId").equals(restaurantA))
                        .map(r -> r.get("restaurantId"))
                        .findFirst();

        java.util.Optional<String> roleBRestaurantId =
                restaurantRoles.stream()
                        .filter(r -> r.get("restaurantId").equals(restaurantB))
                        .map(r -> r.get("restaurantId"))
                        .findFirst();

        assertThat(roleARestaurantId).isPresent();
        assertThat(roleBRestaurantId).isPresent();

        System.out.println("✅ INV-06 PASSED: JWT tokens contain multi-tenant restaurant roles");
    }

    /** INV-06: Repository queries are scoped by restaurant_id. */
    @Test
    void repositoryQueries_ScopedByRestaurantId() {
        // Arrange - Create orders in both restaurants
        TenantContext.setRestaurantId(restaurantA);
        Order orderA1 =
                Order.builder()
                        .id("order-a1")
                        .restaurantId(restaurantA)
                        .num(8010)
                        .type("TAKE_AWAY")
                        .status("PENDING")
                        .total(BigDecimal.valueOf(100))
                        .build();
        orderRepository.save(orderA1);

        TenantContext.setRestaurantId(restaurantB);
        Order orderB1 =
                Order.builder()
                        .id("order-b1")
                        .restaurantId(restaurantB)
                        .num(8011)
                        .type("IN_PLACE")
                        .status("PENDING")
                        .total(BigDecimal.valueOf(200))
                        .build();
        orderRepository.save(orderB1);

        // Act - Query from restaurant A
        TenantContext.setRestaurantId(restaurantA);
        List<Order> restaurantAOrders = orderRepository.findByRestaurantId(restaurantA);

        // Assert - Should only return restaurant A orders
        assertThat(restaurantAOrders).hasSize(2); // orderA + orderA1
        assertThat(restaurantAOrders)
                .allMatch(order -> order.getRestaurantId().equals(restaurantA));

        // Query from restaurant B
        TenantContext.setRestaurantId(restaurantB);
        List<Order> restaurantBOrders = orderRepository.findByRestaurantId(restaurantB);

        assertThat(restaurantBOrders).hasSize(1); // orderB1
        assertThat(restaurantBOrders)
                .allMatch(order -> order.getRestaurantId().equals(restaurantB));

        // Cleanup
        orderRepository.deleteById(orderA1.getId());
        orderRepository.deleteById(orderB1.getId());

        System.out.println("✅ INV-06 PASSED: Repository queries are scoped by restaurant_id");
    }
}
