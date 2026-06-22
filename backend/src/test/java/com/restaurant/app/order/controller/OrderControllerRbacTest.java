package com.restaurant.app.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.config.SecurityConfig;
import com.restaurant.app.order.dto.CreateOrderDetailRequest;
import com.restaurant.app.order.dto.CreateOrderRequest;
import com.restaurant.app.order.dto.OrderDto;
import com.restaurant.app.order.service.OrderService;
import com.restaurant.app.security.JwtAuthenticationFilter;
import com.restaurant.app.security.JwtService;
import com.restaurant.app.security.TenantFilter;
import com.restaurant.app.security.TenantSecurityExpression;
import com.restaurant.app.security.UserDetailsAdapter;
import com.restaurant.app.security.UserDetailsServiceAdapter;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/** PR #2: Verify RBAC enforcement and HTTP semantics for order endpoints. */
@WebMvcTest(OrderController.class)
@ContextConfiguration(
        classes = {
            SecurityConfig.class,
            OrderController.class,
            JwtAuthenticationFilter.class,
            TenantFilter.class,
            TenantSecurityExpression.class
        })
class OrderControllerRbacTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;

    @MockBean private JwtService jwtService;

    @MockBean private UserDetailsServiceAdapter userDetailsService;

    @Test
    void createOrder_WithWaiterRole_Returns201WithLocation() throws Exception {
        OrderDto response = new OrderDto();
        response.setId("order-1");
        response.setNum(1);
        response.setStatus("PENDING");
        response.setTotal(BigDecimal.ZERO);

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("TAKE_AWAY");
        request.setPeople(1);

        CreateOrderDetailRequest detail = new CreateOrderDetailRequest();
        detail.setProductId("product-1");
        detail.setQuantity(1);
        request.setDetails(List.of(detail));

        mockMvc.perform(
                        post("/orders")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", "rest-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/orders/order-1"));

        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void createOrder_WithCookRole_Returns403() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("TAKE_AWAY");
        request.setPeople(1);

        CreateOrderDetailRequest detail = new CreateOrderDetailRequest();
        detail.setProductId("product-1");
        detail.setQuantity(1);
        request.setDetails(List.of(detail));

        mockMvc.perform(
                        post("/orders")
                                .with(user(cookUser()))
                                .header("x-restaurant-id", "rest-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrder_WithoutTenantHeader_Returns400() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("TAKE_AWAY");
        request.setPeople(1);

        CreateOrderDetailRequest detail = new CreateOrderDetailRequest();
        detail.setProductId("product-1");
        detail.setQuantity(1);
        request.setDetails(List.of(detail));

        mockMvc.perform(
                        post("/orders")
                                .with(user(waiterUser()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private UserDetailsAdapter waiterUser() {
        return buildUser("waiter", "WAITER");
    }

    private UserDetailsAdapter cookUser() {
        return buildUser("cook", "COOK");
    }

    private UserDetailsAdapter buildUser(String username, String roleName) {
        Role role = new Role();
        role.setId(1);
        role.setName(roleName);

        UserRestaurantRole restaurantRole = new UserRestaurantRole();
        restaurantRole.setRestaurantId("rest-1");
        restaurantRole.setRole(role);

        return new UserDetailsAdapter(
                "user-1", username, "password", true, List.of(restaurantRole), role);
    }
}
