package com.restaurant.app.menu.service;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.dto.CreateProductionAreaRequest;
import com.restaurant.app.menu.dto.ProductionAreaDto;
import com.restaurant.app.menu.dto.UpdateProductionAreaRequest;
import com.restaurant.app.menu.mapper.ProductionAreaMapper;
import com.restaurant.app.menu.model.ProductionArea;
import com.restaurant.app.menu.repository.ProductionAreaRepository;
import com.restaurant.app.security.TenantContext;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for production area operations. */
@Service
public class ProductionAreaService {

    private final ProductionAreaRepository productionAreaRepository;
    private final ProductionAreaMapper productionAreaMapper;

    public ProductionAreaService(
            ProductionAreaRepository productionAreaRepository,
            ProductionAreaMapper productionAreaMapper) {
        this.productionAreaRepository = productionAreaRepository;
        this.productionAreaMapper = productionAreaMapper;
    }

    /** Get all production areas for current restaurant. */
    @Transactional(readOnly = true)
    public List<ProductionAreaDto> getAllProductionAreas() {
        String restaurantId = TenantContext.getRestaurantId();
        return productionAreaRepository.findAll().stream()
                .filter(area -> restaurantId.equals(area.getRestaurantId()))
                .map(productionAreaMapper::toDto)
                .toList();
    }

    /** Get a production area by ID. */
    @Transactional(readOnly = true)
    public ProductionAreaDto getProductionAreaById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        ProductionArea foundArea =
                productionAreaRepository
                        .findById(id)
                        .filter(area -> restaurantId.equals(area.getRestaurantId()))
                        .orElseThrow(() -> new NotFoundException("ProductionArea", id));
        return productionAreaMapper.toDto(foundArea);
    }

    /** Create a new production area. */
    @Transactional
    public ProductionAreaDto createProductionArea(CreateProductionAreaRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        ProductionArea area =
                ProductionArea.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .name(request.getName())
                        .description(request.getDescription())
                        .build();

        area = productionAreaRepository.save(area);
        return productionAreaMapper.toDto(area);
    }

    /** Update a production area. */
    @Transactional
    public ProductionAreaDto updateProductionArea(String id, UpdateProductionAreaRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        ProductionArea foundArea =
                productionAreaRepository
                        .findById(id)
                        .filter(a -> restaurantId.equals(a.getRestaurantId()))
                        .orElseThrow(() -> new NotFoundException("ProductionArea", id));

        if (request.getName() != null) {
            foundArea.setName(request.getName());
        }

        if (request.getDescription() != null) {
            foundArea.setDescription(request.getDescription());
        }

        foundArea = productionAreaRepository.save(foundArea);
        return productionAreaMapper.toDto(foundArea);
    }

    /** Delete a production area. */
    @Transactional
    public void deleteProductionArea(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        ProductionArea foundArea =
                productionAreaRepository
                        .findById(id)
                        .filter(a -> restaurantId.equals(a.getRestaurantId()))
                        .orElseThrow(() -> new NotFoundException("ProductionArea", id));
        productionAreaRepository.delete(foundArea);
    }
}
