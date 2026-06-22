package com.restaurant.app.table.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.restaurant.app.security.JwtAuthenticationFilter;
import com.restaurant.app.security.JwtService;
import com.restaurant.app.security.TenantFilter;
import com.restaurant.app.security.TenantSecurityExpression;
import com.restaurant.app.security.UserDetailsAdapter;
import com.restaurant.app.security.UserDetailsServiceAdapter;
import com.restaurant.app.table.dto.CreateTableRequest;
import com.restaurant.app.table.dto.TableDto;
import com.restaurant.app.table.dto.UpdateTableRequest;
import com.restaurant.app.table.dto.UpdateTableStatusRequest;
import com.restaurant.app.table.service.TableService;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/** MockMvc tests for {@link TableController} CRUD and status endpoints. */
@WebMvcTest(controllers = TableController.class)
@ContextConfiguration(
        classes = {
            SecurityConfig.class,
            JwtAuthenticationFilter.class,
            TenantFilter.class,
            TenantSecurityExpression.class,
            TableController.class
        })
class TableControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TableService tableService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceAdapter userDetailsService;

    private static final String RESTAURANT_ID = "rest-1";

    @Test
    void getAllTables_WithAdminRole_Returns200() throws Exception {
        when(tableService.getAllTables()).thenReturn(List.of(tableDto("table-1")));

        mockMvc.perform(
                        get("/tables")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("table-1"));

        verify(tableService).getAllTables();
    }

    @Test
    void getTableById_WithAdminRole_Returns200() throws Exception {
        when(tableService.getTableById("table-1")).thenReturn(tableDto("table-1"));

        mockMvc.perform(
                        get("/tables/table-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("table-1"));

        verify(tableService).getTableById("table-1");
    }

    @Test
    void createTable_WithAdminRole_Returns201WithLocation() throws Exception {
        when(tableService.createTable(any(CreateTableRequest.class)))
                .thenReturn(tableDto("table-new"));

        CreateTableRequest request = new CreateTableRequest();
        request.setNum(1);
        request.setSeats(4);

        mockMvc.perform(
                        post("/tables")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        HttpHeaders.LOCATION,
                                        Matchers.containsString("/tables/table-new")));

        verify(tableService).createTable(any(CreateTableRequest.class));
    }

    @Test
    void createTable_WithWaiterRole_Returns403() throws Exception {
        CreateTableRequest request = new CreateTableRequest();
        request.setNum(1);
        request.setSeats(4);

        mockMvc.perform(
                        post("/tables")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTable_WithAdminRole_Returns200() throws Exception {
        when(tableService.updateTable(any(String.class), any(UpdateTableRequest.class)))
                .thenReturn(tableDto("table-1"));

        UpdateTableRequest request = new UpdateTableRequest();
        request.setSeats(6);

        mockMvc.perform(
                        put("/tables/table-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("table-1"));

        verify(tableService).updateTable(any(String.class), any(UpdateTableRequest.class));
    }

    @Test
    void updateTableStatus_WithWaiterRole_Returns200() throws Exception {
        TableDto updated = tableDto("table-1");
        updated.setStatus("OCCUPIED");
        when(tableService.updateTableStatus(any(String.class), any(UpdateTableStatusRequest.class)))
                .thenReturn(updated);

        UpdateTableStatusRequest request = new UpdateTableStatusRequest();
        request.setStatus("OCCUPIED");

        mockMvc.perform(
                        put("/tables/table-1/status")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OCCUPIED"));

        verify(tableService)
                .updateTableStatus(any(String.class), any(UpdateTableStatusRequest.class));
    }

    @Test
    void deleteTable_WithAdminRole_Returns204() throws Exception {
        mockMvc.perform(
                        delete("/tables/table-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isNoContent());

        verify(tableService).deleteTable("table-1");
    }

    @Test
    void protectedEndpoint_WithoutTenantHeader_Returns400() throws Exception {
        mockMvc.perform(get("/tables").with(user(adminUser()))).andExpect(status().isBadRequest());
    }

    private TableDto tableDto(String id) {
        TableDto dto = new TableDto();
        dto.setId(id);
        dto.setNum(1);
        dto.setSeats(4);
        dto.setStatus("AVAILABLE");
        dto.setActive(true);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    private UserDetailsAdapter adminUser() {
        return buildUser("admin-1", "admin", "ADMIN");
    }

    private UserDetailsAdapter waiterUser() {
        return buildUser("waiter-1", "waiter", "WAITER");
    }

    private UserDetailsAdapter buildUser(String id, String username, String roleName) {
        Role role = new Role();
        role.setId(1);
        role.setName(roleName);

        UserRestaurantRole restaurantRole = new UserRestaurantRole();
        restaurantRole.setRestaurantId(RESTAURANT_ID);
        restaurantRole.setRole(role);

        return new UserDetailsAdapter(
                id, username, "password", true, List.of(restaurantRole), role);
    }
}
