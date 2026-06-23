package com.restaurant.app.menu.controller;

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
import com.restaurant.app.menu.dto.CategoryDto;
import com.restaurant.app.menu.dto.ProductDto;
import com.restaurant.app.menu.dto.ProductOptionDto;
import com.restaurant.app.menu.dto.SectionDto;
import com.restaurant.app.menu.service.CategoryService;
import com.restaurant.app.menu.service.ProductOptionService;
import com.restaurant.app.menu.service.ProductService;
import com.restaurant.app.menu.service.SectionService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/** MockMvc read tests for menu controllers (section, category, product, product option). */
@WebMvcTest(
        controllers = {
            SectionController.class,
            CategoryController.class,
            ProductController.class,
            ProductOptionController.class
        })
@ContextConfiguration(
        classes = {
            SecurityConfig.class,
            JwtAuthenticationFilter.class,
            TenantFilter.class,
            TenantSecurityExpression.class,
            SectionController.class,
            CategoryController.class,
            ProductController.class,
            ProductOptionController.class
        })
class MenuControllersTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private SectionService sectionService;
    @MockBean private CategoryService categoryService;
    @MockBean private ProductService productService;
    @MockBean private ProductOptionService productOptionService;

    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceAdapter userDetailsService;

    private static final String RESTAURANT_ID = "rest-1";

    @Test
    void sectionReadEndpoints_WithAdminRole_Return200AndInvokeService() throws Exception {
        when(sectionService.getAllSections()).thenReturn(List.of(sectionDto("section-1")));
        when(sectionService.getActiveSections()).thenReturn(List.of(sectionDto("section-2")));
        when(sectionService.getSectionById("section-1")).thenReturn(sectionDto("section-1"));

        mockMvc.perform(
                        get("/sections")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("section-1"));
        mockMvc.perform(
                        get("/sections/active")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("section-2"));
        mockMvc.perform(
                        get("/sections/section-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("section-1"));

        verify(sectionService).getAllSections();
        verify(sectionService).getActiveSections();
        verify(sectionService).getSectionById("section-1");
    }

    @Test
    void categoryReadEndpoints_WithAdminRole_Return200AndInvokeService() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryDto("category-1")));
        when(categoryService.getActiveCategories()).thenReturn(List.of(categoryDto("category-2")));
        when(categoryService.getCategoryById("category-1")).thenReturn(categoryDto("category-1"));

        mockMvc.perform(
                        get("/categories")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("category-1"));
        mockMvc.perform(
                        get("/categories/active")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("category-2"));
        mockMvc.perform(
                        get("/categories/category-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("category-1"));

        verify(categoryService).getAllCategories();
        verify(categoryService).getActiveCategories();
        verify(categoryService).getCategoryById("category-1");
    }

    @Test
    void productReadEndpoints_WithAdminRole_Return200AndInvokeService() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productDto("product-1")));
        when(productService.getActiveProducts()).thenReturn(List.of(productDto("product-2")));
        when(productService.getAvailableProducts()).thenReturn(List.of(productDto("product-3")));
        when(productService.getProductsByCategory("category-1"))
                .thenReturn(List.of(productDto("product-1")));
        when(productService.getProductById("product-1")).thenReturn(productDto("product-1"));

        mockMvc.perform(
                        get("/products")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("product-1"));
        mockMvc.perform(
                        get("/products/active")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("product-2"));
        mockMvc.perform(
                        get("/products/available")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("product-3"));
        mockMvc.perform(
                        get("/products/category/category-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("product-1"));
        mockMvc.perform(
                        get("/products/product-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-1"));

        verify(productService).getAllProducts();
        verify(productService).getActiveProducts();
        verify(productService).getAvailableProducts();
        verify(productService).getProductsByCategory("category-1");
        verify(productService).getProductById("product-1");
    }

    @Test
    void productOptionReadEndpoints_WithAdminRole_Return200AndInvokeService() throws Exception {
        when(productOptionService.getAllProductOptions())
                .thenReturn(List.of(productOptionDto("option-1")));
        when(productOptionService.getOptionsByProduct("product-1"))
                .thenReturn(List.of(productOptionDto("option-1")));
        when(productOptionService.getAvailableOptionsByProduct("product-1"))
                .thenReturn(List.of(productOptionDto("option-1")));
        when(productOptionService.getProductOptionById("option-1"))
                .thenReturn(productOptionDto("option-1"));

        mockMvc.perform(
                        get("/product-options")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("option-1"));
        mockMvc.perform(
                        get("/product-options/product/product-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("option-1"));
        mockMvc.perform(
                        get("/product-options/product/product-1/available")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("option-1"));
        mockMvc.perform(
                        get("/product-options/option-1")
                                .with(user(adminUser()))
                                .header("x-restaurant-id", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("option-1"));

        verify(productOptionService).getAllProductOptions();
        verify(productOptionService).getOptionsByProduct("product-1");
        verify(productOptionService).getAvailableOptionsByProduct("product-1");
        verify(productOptionService).getProductOptionById("option-1");
    }

    private SectionDto sectionDto(String id) {
        return new SectionDto(id, RESTAURANT_ID, "Section", null, 1, true);
    }

    private CategoryDto categoryDto(String id) {
        return new CategoryDto(
                id,
                "Category",
                "Description",
                "section-1",
                true,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private ProductDto productDto(String id) {
        return new ProductDto(
                id,
                "Product",
                "Description",
                BigDecimal.valueOf(50),
                "category-1",
                BigDecimal.valueOf(0.16),
                10,
                false,
                "AVAILABLE",
                null,
                5,
                true,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private ProductOptionDto productOptionDto(String id) {
        ProductOptionDto dto = new ProductOptionDto();
        dto.setId(id);
        dto.setName("Extra");
        dto.setPriceAdjustment(BigDecimal.valueOf(5));
        dto.setProductId("product-1");
        dto.setDefault(false);
        dto.setAvailable(true);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
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
