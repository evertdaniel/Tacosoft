package com.restaurant.app.supplier.service;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.security.TenantContext;
import com.restaurant.app.supplier.dto.CreateSupplierRequest;
import com.restaurant.app.supplier.dto.SupplierDto;
import com.restaurant.app.supplier.dto.UpdateSupplierRequest;
import com.restaurant.app.supplier.mapper.SupplierMapper;
import com.restaurant.app.supplier.model.Supplier;
import com.restaurant.app.supplier.repository.SupplierRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for supplier operations. Implements T10.1. */
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    /** Get all suppliers for current restaurant. */
    @Transactional(readOnly = true)
    public List<SupplierDto> getAllSuppliers() {
        String restaurantId = TenantContext.getRestaurantId();
        return supplierRepository.findByRestaurantId(restaurantId).stream()
                .map(supplierMapper::toDto)
                .toList();
    }

    /** Get active suppliers only. */
    @Transactional(readOnly = true)
    public List<SupplierDto> getActiveSuppliers() {
        String restaurantId = TenantContext.getRestaurantId();
        return supplierRepository.findByRestaurantIdAndIsActive(restaurantId, true).stream()
                .map(supplierMapper::toDto)
                .toList();
    }

    /** Get a supplier by ID. */
    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Supplier supplier =
                supplierRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Supplier", id));
        return supplierMapper.toDto(supplier);
    }

    /** Search suppliers by name. */
    @Transactional(readOnly = true)
    public List<SupplierDto> searchSuppliersByName(String name) {
        String restaurantId = TenantContext.getRestaurantId();
        return supplierRepository.findByRestaurantIdAndNameContaining(restaurantId, name).stream()
                .map(supplierMapper::toDto)
                .toList();
    }

    /** Create a new supplier. */
    @Transactional
    public SupplierDto createSupplier(CreateSupplierRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        Supplier supplier =
                Supplier.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .name(request.getName())
                        .contactName(request.getContactName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .address(request.getAddress())
                        .taxId(request.getTaxId())
                        .isActive(true)
                        .build();

        supplier = supplierRepository.save(supplier);
        return supplierMapper.toDto(supplier);
    }

    /** Update a supplier. */
    @Transactional
    public SupplierDto updateSupplier(String id, UpdateSupplierRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        Supplier supplier =
                supplierRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Supplier", id));

        if (request.getName() != null) {
            supplier.setName(request.getName());
        }

        if (request.getContactName() != null) {
            supplier.setContactName(request.getContactName());
        }

        if (request.getEmail() != null) {
            supplier.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            supplier.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            supplier.setAddress(request.getAddress());
        }

        if (request.getTaxId() != null) {
            supplier.setTaxId(request.getTaxId());
        }

        if (request.getIsActive() != null) {
            supplier.setIsActive(request.getIsActive());
        }

        supplier = supplierRepository.save(supplier);
        return supplierMapper.toDto(supplier);
    }

    /** Delete a supplier. */
    @Transactional
    public void deleteSupplier(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Supplier supplier =
                supplierRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Supplier", id));
        supplierRepository.delete(supplier);
    }
}
