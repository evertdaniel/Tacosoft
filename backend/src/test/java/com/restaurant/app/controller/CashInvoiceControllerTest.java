package com.restaurant.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import com.restaurant.app.billing.controller.InvoiceController;
import com.restaurant.app.billing.dto.CreateInvoiceRequest;
import com.restaurant.app.billing.dto.InvoiceDto;
import com.restaurant.app.billing.dto.PaymentRequest;
import com.restaurant.app.billing.service.InvoiceService;
import com.restaurant.app.cash.controller.CashRegisterController;
import com.restaurant.app.cash.dto.CashRegisterDto;
import com.restaurant.app.cash.dto.CloseCashRegisterRequest;
import com.restaurant.app.cash.dto.OpenCashRegisterRequest;
import com.restaurant.app.cash.dto.XReportDto;
import com.restaurant.app.cash.dto.ZReportDto;
import com.restaurant.app.cash.service.CashRegisterService;
import com.restaurant.app.config.SecurityConfig;
import com.restaurant.app.security.JwtAuthenticationFilter;
import com.restaurant.app.security.JwtService;
import com.restaurant.app.security.TenantFilter;
import com.restaurant.app.security.TenantSecurityExpression;
import com.restaurant.app.security.UserDetailsAdapter;
import com.restaurant.app.security.UserDetailsServiceAdapter;
import java.math.BigDecimal;
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

/**
 * MockMvc tests for {@link CashRegisterController} and {@link InvoiceController}. Covers
 * open/close, X/Z reports, invoice creation, payment and list endpoints with RBAC and tenant
 * validation.
 */
@WebMvcTest(controllers = {CashRegisterController.class, InvoiceController.class})
@ContextConfiguration(
        classes = {
            SecurityConfig.class,
            JwtAuthenticationFilter.class,
            TenantFilter.class,
            TenantSecurityExpression.class,
            CashRegisterController.class,
            InvoiceController.class
        })
class CashInvoiceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CashRegisterService cashRegisterService;
    @MockBean private InvoiceService invoiceService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceAdapter userDetailsService;

    private static final String RESTAURANT_ID = "rest-1";

    @Test
    void cashRegisterEndpoints_WithCashierRole_Returns201And200AndInvokeService() throws Exception {
        CashRegisterDto register = cashRegisterDto("register-1", "OPEN");
        ZReportDto zReport = zReportDto("register-1");
        XReportDto xReport = xReportDto("register-1");

        when(cashRegisterService.openRegister(any(OpenCashRegisterRequest.class), anyString()))
                .thenReturn(register);
        when(cashRegisterService.closeRegister(
                        anyString(), any(CloseCashRegisterRequest.class), anyString()))
                .thenReturn(zReport);
        when(cashRegisterService.getActiveRegister(anyString())).thenReturn(register);
        when(cashRegisterService.getXReport(anyString())).thenReturn(xReport);
        when(cashRegisterService.listRegisters()).thenReturn(List.of(register));

        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setOpeningAmount(BigDecimal.valueOf(100));

        mockMvc.perform(
                        post("/cash-registers/open")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(openRequest)))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        HttpHeaders.LOCATION,
                                        Matchers.containsString("/cash-registers/open/register-1")))
                .andExpect(jsonPath("$.id").value("register-1"));

        CloseCashRegisterRequest closeRequest = new CloseCashRegisterRequest();
        closeRequest.setClosingAmount(BigDecimal.valueOf(100));

        mockMvc.perform(
                        put("/cash-registers/register-1/close")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(closeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashRegisterId").value("register-1"));
        mockMvc.perform(
                        get("/cash-registers/active")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("register-1"));
        mockMvc.perform(
                        get("/cash-registers/x-report")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashRegisterId").value("register-1"));
        mockMvc.perform(
                        get("/cash-registers")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("register-1"));

        verify(cashRegisterService).openRegister(any(OpenCashRegisterRequest.class), anyString());
        verify(cashRegisterService)
                .closeRegister(anyString(), any(CloseCashRegisterRequest.class), anyString());
        verify(cashRegisterService).getActiveRegister(anyString());
        verify(cashRegisterService).getXReport(anyString());
        verify(cashRegisterService).listRegisters();
    }

    @Test
    void openCashRegister_WithWaiterRole_Returns403() throws Exception {
        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setOpeningAmount(BigDecimal.valueOf(100));

        mockMvc.perform(
                        post("/cash-registers/open")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(openRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cashRegisterEndpoint_WithoutTenantHeader_Returns400() throws Exception {
        mockMvc.perform(
                        get("/cash-registers")
                                .with(user(cashierUser()))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invoiceEndpoints_WithCashierRole_Returns201And200AndInvokeService() throws Exception {
        InvoiceDto invoice = invoiceDto("invoice-1", 100L, false);
        InvoiceDto paidInvoice = invoiceDto("invoice-1", 100L, true);

        when(invoiceService.createInvoice(any(CreateInvoiceRequest.class))).thenReturn(invoice);
        when(invoiceService.payInvoice(anyString(), any(PaymentRequest.class), anyString()))
                .thenReturn(paidInvoice);
        when(invoiceService.getInvoice("invoice-1")).thenReturn(invoice);
        when(invoiceService.listInvoices()).thenReturn(List.of(invoice));
        when(invoiceService.listUnpaidInvoices()).thenReturn(List.of(invoice));

        CreateInvoiceRequest createRequest = new CreateInvoiceRequest();
        createRequest.setOrderId("order-1");

        mockMvc.perform(
                        post("/invoices")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        HttpHeaders.LOCATION,
                                        Matchers.containsString("/invoices/invoice-1")))
                .andExpect(jsonPath("$.folio").value(100));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(BigDecimal.valueOf(100));
        paymentRequest.setPaymentMethod("CASH");

        mockMvc.perform(
                        post("/invoices/invoice-1/pay")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPaid").value(true));
        mockMvc.perform(
                        get("/invoices/invoice-1")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("invoice-1"));
        mockMvc.perform(
                        get("/invoices")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("invoice-1"));
        mockMvc.perform(
                        get("/invoices/unpaid")
                                .with(user(cashierUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("invoice-1"));

        verify(invoiceService).createInvoice(any(CreateInvoiceRequest.class));
        verify(invoiceService).payInvoice(anyString(), any(PaymentRequest.class), anyString());
        verify(invoiceService).getInvoice("invoice-1");
        verify(invoiceService).listInvoices();
        verify(invoiceService).listUnpaidInvoices();
    }

    @Test
    void createInvoice_WithWaiterRole_Returns403() throws Exception {
        CreateInvoiceRequest createRequest = new CreateInvoiceRequest();
        createRequest.setOrderId("order-1");

        mockMvc.perform(
                        post("/invoices")
                                .with(user(waiterUser()))
                                .header("x-restaurant-id", RESTAURANT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void invoiceEndpoint_WithoutTenantHeader_Returns400() throws Exception {
        mockMvc.perform(
                        get("/invoices")
                                .with(user(cashierUser()))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private CashRegisterDto cashRegisterDto(String id, String status) {
        return CashRegisterDto.builder()
                .id(id)
                .restaurantId(RESTAURANT_ID)
                .userId("user-1")
                .openingAmount(BigDecimal.valueOf(100))
                .status(status)
                .openedAt(LocalDateTime.now())
                .build();
    }

    private XReportDto xReportDto(String registerId) {
        return XReportDto.builder()
                .cashRegisterId(registerId)
                .currentBalance(BigDecimal.valueOf(100))
                .openingAmount(BigDecimal.valueOf(100))
                .totalIncome(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .transactionCount(0)
                .incomeCount(0)
                .expenseCount(0)
                .build();
    }

    private ZReportDto zReportDto(String registerId) {
        return ZReportDto.builder()
                .cashRegisterId(registerId)
                .openingAmount(BigDecimal.valueOf(100))
                .expectedAmount(BigDecimal.valueOf(100))
                .declaredAmount(BigDecimal.valueOf(100))
                .difference(BigDecimal.ZERO)
                .totalIncome(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .incomeCount(0)
                .expenseCount(0)
                .status("BALANCED")
                .build();
    }

    private InvoiceDto invoiceDto(String id, Long folio, boolean isPaid) {
        return InvoiceDto.builder()
                .id(id)
                .restaurantId(RESTAURANT_ID)
                .orderId("order-1")
                .folio(folio)
                .subtotal(BigDecimal.valueOf(86.21))
                .tax(BigDecimal.valueOf(13.79))
                .total(BigDecimal.valueOf(100))
                .isPaid(isPaid)
                .paymentMethod(isPaid ? "CASH" : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserDetailsAdapter adminUser() {
        return buildUser("admin-1", "ADMIN");
    }

    private UserDetailsAdapter waiterUser() {
        return buildUser("waiter-1", "WAITER");
    }

    private UserDetailsAdapter cashierUser() {
        return buildUser("cashier-1", "CASHIER");
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
