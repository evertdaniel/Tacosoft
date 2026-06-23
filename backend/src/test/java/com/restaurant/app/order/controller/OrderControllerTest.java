package com.restaurant.app.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.config.SecurityConfig;
import com.restaurant.app.order.dto.CreateOrderDetailRequest;
import com.restaurant.app.order.dto.CreateOrderRequest;
import com.restaurant.app.order.dto.OrderDetailDto;
import com.restaurant.app.order.dto.OrderDto;
import com.restaurant.app.order.dto.UpdateOrderDetailStatusRequest;
import com.restaurant.app.order.service.OrderDetailService;
import com.restaurant.app.order.service.OrderService;
import com.restaurant.app.security.JwtAuthenticationFilter;
import com.restaurant.app.security.JwtService;
import com.restaurant.app.security.TenantFilter;
import com.restaurant.app.security.TenantSecurityExpression;
import com.restaurant.app.security.UserDetailsAdapter;
import com.restaurant.app.security.UserDetailsServiceAdapter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc tests for {@link OrderController} and {@link OrderDetailController}. Covers happy paths,
 * RBAC and tenant header validation.
 */
@WebMvcTest(controllers = {OrderController.class, OrderDetailController.class})
@ContextConfiguration(
        classes = {
            SecurityConfig.class,
            JwtAuthenticationFilter.class,
            TenantFilter.class,
            TenantSecurityExpression.class,
            OrderController.class,
            OrderDetailController.class
        })
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private OrderDetailService orderDetailService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceAdapter userDetailsService;

    private static final String RESTAURANT_ID = "rest-1";

    @Test
    void orderEndpoints_WithWaiterRole_Returns201And200AndInvokeService() throws Exception {
        OrderDto order = orderDto("order-1", 1, "PENDING");
        OrderDetailDto detail = orderDetailDto("detail-1");

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderService.getAllOrders()).thenReturn(List.of(order));
        when(orderService.getActiveOrders()).thenReturn(List.of(order));
        when(orderService.getOrdersByStatus("PENDING")).thenReturn(List.of(order));
        when(orderService.getOrderById("order-1")).thenReturn(order);
        when(orderService.updateOrder(anyString(), any(CreateOrderRequest.class)))
                .thenReturn(order);
        when(orderDetailService.updateOrderDetailStatus(
                        anyString(), any(UpdateOrderDetailStatusRequest.class)))
                .thenReturn(detail);

        mockMvc.perform(
                        post("/orders")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createOrderRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/orders/order-1"))
                .andExpect(jsonPath("$.id").value("order-1"));

        mockMvc.perform(
                        get("/orders")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("order-1"));
        mockMvc.perform(
                        get("/orders/active")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("order-1"));
        mockMvc.perform(
                        get("/orders/status/PENDING")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("order-1"));
        mockMvc.perform(
                        get("/orders/order-1")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-1"));
        mockMvc.perform(
                        put("/orders/order-1")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createOrderRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-1"));
        mockMvc.perform(
                        put("/order-details/detail-1/status")
                                .with(user(cookUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateOrderDetailStatusRequest("IN_PROGRESS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("detail-1"));
        mockMvc.perform(
                        delete("/orders/order-1")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isNoContent());

        verify(orderService).createOrder(any(CreateOrderRequest.class));
        verify(orderService).getAllOrders();
        verify(orderService).getActiveOrders();
        verify(orderService).getOrdersByStatus("PENDING");
        verify(orderService).getOrderById("order-1");
        verify(orderService).updateOrder(anyString(), any(CreateOrderRequest.class));
        verify(orderDetailService)
                .updateOrderDetailStatus(anyString(), any(UpdateOrderDetailStatusRequest.class));
        verify(orderService).deleteOrder("order-1");
    }

    @Test
    void createOrder_WithCookRole_Returns403() throws Exception {
        mockMvc.perform(
                        post("/orders")
                                .with(user(cookUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createOrderRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void orderEndpoint_WithoutTenantHeader_Returns400() throws Exception {
        mockMvc.perform(
                        get("/orders")
                                .with(user(waiterUser()))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private CreateOrderRequest createOrderRequest() {
        CreateOrderDetailRequest detail = new CreateOrderDetailRequest();
        detail.setProductId("product-1");
        detail.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setType("TAKE_AWAY");
        request.setPeople(1);
        request.setDetails(List.of(detail));
        return request;
    }

    private UpdateOrderDetailStatusRequest updateOrderDetailStatusRequest(String status) {
        UpdateOrderDetailStatusRequest request = new UpdateOrderDetailStatusRequest();
        request.setStatus(status);
        return request;
    }

    private OrderDto orderDto(String id, int num, String status) {
        OrderDto dto = new OrderDto();
        dto.setId(id);
        dto.setNum(num);
        dto.setType("TAKE_AWAY");
        dto.setStatus(status);
        dto.setTotal(BigDecimal.valueOf(100));
        dto.setPeople(1);
        dto.setDetails(List.of());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    private OrderDetailDto orderDetailDto(String id) {
        return new OrderDetailDto(
                id,
                "order-1",
                "product-1",
                "Product",
                1,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                "PENDING",
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private UserDetailsAdapter waiterUser() {
        return buildUser("waiter-1", "WAITER");
    }

    private UserDetailsAdapter cookUser() {
        return buildUser("cook-1", "COOK");
    }

    private UserDetailsAdapter buildUser(String id, String roleName) {
        Role role = new Role();
        role.setId(1);
        role.setName(roleName);

        UserRestaurantRole restaurantRole = new UserRestaurantRole();
        restaurantRole.setRestaurantId(RESTAURANT_ID);
        restaurantRole.setRole(role);

        return new UserDetailsAdapter(
                id, roleName.toLowerCase(), "password", true, List.of(restaurantRole), role);
    }
}
