package com.restaurant.app.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.app.auth.dto.LoginRequest;
import com.restaurant.app.auth.dto.LoginResponse;
import com.restaurant.app.auth.service.AuthService;
import com.restaurant.app.config.SecurityConfig;
import com.restaurant.app.security.JwtAuthenticationFilter;
import com.restaurant.app.security.JwtService;
import com.restaurant.app.security.TenantFilter;
import com.restaurant.app.security.TenantSecurityExpression;
import com.restaurant.app.security.UserDetailsServiceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/** MockMvc tests for {@link AuthController} login endpoints. */
@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(
        classes = {
            SecurityConfig.class,
            JwtAuthenticationFilter.class,
            TenantFilter.class,
            TenantSecurityExpression.class,
            AuthController.class
        })
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceAdapter userDetailsService;

    @Test
    void login_ValidCredentials_Returns200WithToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-token", null, null));

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new LoginRequest("admin", "pass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_InvalidRequest_Returns400() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest("", ""))))
                .andExpect(status().isBadRequest());
    }
}
