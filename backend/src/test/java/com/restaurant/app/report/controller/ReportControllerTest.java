package com.restaurant.app.report.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.config.SecurityConfig;
import com.restaurant.app.report.dto.DashboardReportDto;
import com.restaurant.app.report.dto.SalesSummaryDto;
import com.restaurant.app.report.service.ReportService;
import com.restaurant.app.security.JwtAuthenticationFilter;
import com.restaurant.app.security.JwtService;
import com.restaurant.app.security.TenantFilter;
import com.restaurant.app.security.TenantSecurityExpression;
import com.restaurant.app.security.UserDetailsAdapter;
import com.restaurant.app.security.UserDetailsServiceAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/** MockMvc read tests for {@link ReportController}. */
@WebMvcTest(controllers = ReportController.class)
@ContextConfiguration(
        classes = {
            SecurityConfig.class,
            JwtAuthenticationFilter.class,
            TenantFilter.class,
            TenantSecurityExpression.class,
            ReportController.class
        })
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ReportService reportService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceAdapter userDetailsService;

    private static final String RESTAURANT_ID = "rest-1";

    @Test
    void reportReadEndpoints_WithAdminRole_Return200AndInvokeService() throws Exception {
        when(reportService.getDashboard())
                .thenReturn(DashboardReportDto.builder().occupiedTables(2).activeOrders(3).build());
        when(reportService.getSalesReport(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(
                        SalesSummaryDto.builder().totalRevenue(BigDecimal.valueOf(100)).build());

        mockMvc.perform(
                        get("/reports/dashboard")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupiedTables").value(2))
                .andExpect(jsonPath("$.activeOrders").value(3));
        mockMvc.perform(
                        get("/reports/sales")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(100));

        verify(reportService).getDashboard();
        verify(reportService).getSalesReport(any(LocalDate.class), any(LocalDate.class));
    }

    private UserDetailsAdapter adminUser() {
        Role role = new Role();
        role.setId(1);
        role.setName("ADMIN");

        UserRestaurantRole restaurantRole = new UserRestaurantRole();
        restaurantRole.setRestaurantId(RESTAURANT_ID);
        restaurantRole.setRole(role);

        return new UserDetailsAdapter(
                "admin-1", "admin", "password", true, List.of(restaurantRole), role);
    }
}
