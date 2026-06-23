package com.restaurant.app.supplier.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.security.TenantContext;
import com.restaurant.app.supplier.dto.CreateSupplierRequest;
import com.restaurant.app.supplier.dto.SupplierDto;
import com.restaurant.app.supplier.dto.UpdateSupplierRequest;
import com.restaurant.app.supplier.mapper.SupplierMapper;
import com.restaurant.app.supplier.model.Supplier;
import com.restaurant.app.supplier.repository.SupplierRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link SupplierService}. */
@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock private SupplierRepository supplierRepository;

    @Mock private SupplierMapper supplierMapper;

    @InjectMocks private SupplierService supplierService;

    private final String restaurantId = "restaurant-1";
    private final String supplierId = "supplier-1";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAllSuppliers_ReturnsSuppliersForRestaurant() {
        Supplier supplier = supplierEntity(supplierId, "Acme");
        when(supplierRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(supplier));
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDto(supplierId));

        List<SupplierDto> result = supplierService.getAllSuppliers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(supplierId);
        verify(supplierRepository).findByRestaurantId(restaurantId);
    }

    @Test
    void getActiveSuppliers_ReturnsOnlyActiveSuppliers() {
        Supplier supplier = supplierEntity(supplierId, "Acme");
        when(supplierRepository.findByRestaurantIdAndIsActive(restaurantId, true))
                .thenReturn(List.of(supplier));
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDto(supplierId));

        List<SupplierDto> result = supplierService.getActiveSuppliers();

        assertThat(result).hasSize(1);
        verify(supplierRepository).findByRestaurantIdAndIsActive(restaurantId, true);
    }

    @Test
    void getSupplierById_ExistingSupplier_ReturnsDto() {
        Supplier supplier = supplierEntity(supplierId, "Acme");
        when(supplierRepository.findByIdAndRestaurantId(supplierId, restaurantId))
                .thenReturn(Optional.of(supplier));
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDto(supplierId));

        SupplierDto result = supplierService.getSupplierById(supplierId);

        assertThat(result.getId()).isEqualTo(supplierId);
    }

    @Test
    void getSupplierById_SupplierNotFound_ThrowsNotFoundException() {
        when(supplierRepository.findByIdAndRestaurantId(supplierId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getSupplierById(supplierId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Supplier");
    }

    @Test
    void searchSuppliersByName_ReturnsMatchingSuppliers() {
        Supplier supplier = supplierEntity(supplierId, "Acme");
        when(supplierRepository.findByRestaurantIdAndNameContaining(restaurantId, "ac"))
                .thenReturn(List.of(supplier));
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDto(supplierId));

        List<SupplierDto> result = supplierService.searchSuppliersByName("ac");

        assertThat(result).hasSize(1);
        verify(supplierRepository).findByRestaurantIdAndNameContaining(restaurantId, "ac");
    }

    @Test
    void createSupplier_ValidRequest_SavesAndReturnsDto() {
        CreateSupplierRequest request = createRequest();
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(supplierMapper.toDto(any(Supplier.class))).thenReturn(supplierDto(supplierId));

        SupplierDto result = supplierService.createSupplier(request);

        assertThat(result.getId()).isEqualTo(supplierId);
        verify(supplierRepository)
                .save(
                        argThat(
                                s ->
                                        s.getName().equals("Acme")
                                                && s.getRestaurantId().equals(restaurantId)
                                                && Boolean.TRUE.equals(s.getIsActive())));
    }

    @Test
    void updateSupplier_ExistingSupplier_UpdatesAllFields() {
        Supplier supplier = supplierEntity(supplierId, "Acme");
        UpdateSupplierRequest request = updateRequest();
        when(supplierRepository.findByIdAndRestaurantId(supplierId, restaurantId))
                .thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDto(supplierId));

        SupplierDto result = supplierService.updateSupplier(supplierId, request);

        assertThat(supplier.getName()).isEqualTo("New Acme");
        assertThat(supplier.getContactName()).isEqualTo("John");
        assertThat(supplier.getEmail()).isEqualTo("john@acme.com");
        assertThat(supplier.getPhone()).isEqualTo("555-1234");
        assertThat(supplier.getAddress()).isEqualTo("123 Main");
        assertThat(supplier.getTaxId()).isEqualTo("TAX-1");
        assertThat(supplier.getIsActive()).isFalse();
        assertThat(result.getId()).isEqualTo(supplierId);
    }

    @Test
    void updateSupplier_PartialFields_UpdatesOnlyProvidedFields() {
        Supplier supplier = supplierEntity(supplierId, "Acme");
        UpdateSupplierRequest request = new UpdateSupplierRequest();
        request.setName("New Acme");
        when(supplierRepository.findByIdAndRestaurantId(supplierId, restaurantId))
                .thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDto(supplierId));

        supplierService.updateSupplier(supplierId, request);

        assertThat(supplier.getName()).isEqualTo("New Acme");
        assertThat(supplier.getPhone()).isEqualTo("555-0000");
        assertThat(supplier.getIsActive()).isTrue();
    }

    @Test
    void updateSupplier_SupplierNotFound_ThrowsNotFoundException() {
        UpdateSupplierRequest request = new UpdateSupplierRequest();
        request.setName("New Acme");
        when(supplierRepository.findByIdAndRestaurantId(supplierId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.updateSupplier(supplierId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Supplier");
    }

    @Test
    void deleteSupplier_ExistingSupplier_DeletesSuccessfully() {
        Supplier supplier = supplierEntity(supplierId, "Acme");
        when(supplierRepository.findByIdAndRestaurantId(supplierId, restaurantId))
                .thenReturn(Optional.of(supplier));

        supplierService.deleteSupplier(supplierId);

        verify(supplierRepository).delete(supplier);
    }

    @Test
    void deleteSupplier_SupplierNotFound_ThrowsNotFoundException() {
        when(supplierRepository.findByIdAndRestaurantId(supplierId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.deleteSupplier(supplierId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Supplier");
    }

    private CreateSupplierRequest createRequest() {
        CreateSupplierRequest request = new CreateSupplierRequest();
        request.setName("Acme");
        request.setContactName("Contact");
        request.setEmail("acme@example.com");
        request.setPhone("555-0000");
        request.setAddress("Address");
        request.setTaxId("TAX");
        return request;
    }

    private UpdateSupplierRequest updateRequest() {
        UpdateSupplierRequest request = new UpdateSupplierRequest();
        request.setName("New Acme");
        request.setContactName("John");
        request.setEmail("john@acme.com");
        request.setPhone("555-1234");
        request.setAddress("123 Main");
        request.setTaxId("TAX-1");
        request.setIsActive(false);
        return request;
    }

    private Supplier supplierEntity(String id, String name) {
        return Supplier.builder()
                .id(id)
                .restaurantId(restaurantId)
                .name(name)
                .contactName("Contact")
                .email("acme@example.com")
                .phone("555-0000")
                .address("Address")
                .taxId("TAX")
                .isActive(true)
                .build();
    }

    private SupplierDto supplierDto(String id) {
        return SupplierDto.builder()
                .id(id)
                .restaurantId(restaurantId)
                .name("Acme")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
