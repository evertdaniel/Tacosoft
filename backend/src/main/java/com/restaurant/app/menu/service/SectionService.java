package com.restaurant.app.menu.service;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.dto.CreateSectionRequest;
import com.restaurant.app.menu.dto.SectionDto;
import com.restaurant.app.menu.dto.UpdateSectionRequest;
import com.restaurant.app.menu.mapper.SectionMapper;
import com.restaurant.app.menu.model.Section;
import com.restaurant.app.menu.repository.SectionRepository;
import com.restaurant.app.security.TenantContext;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for Section CRUD operations. */
@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;

    public SectionService(SectionRepository sectionRepository, SectionMapper sectionMapper) {
        this.sectionRepository = sectionRepository;
        this.sectionMapper = sectionMapper;
    }

    /** Create a new section. */
    @Transactional
    public SectionDto createSection(CreateSectionRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        Section section =
                Section.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .name(request.name())
                        .description(request.description())
                        .displayOrder(request.displayOrder())
                        .isActive(request.isActive())
                        .build();

        Section saved = sectionRepository.save(section);
        return sectionMapper.toDto(saved);
    }

    /** Get all sections for the current restaurant. */
    @Transactional(readOnly = true)
    public List<SectionDto> getAllSections() {
        String restaurantId = TenantContext.getRestaurantId();
        List<Section> sections =
                sectionRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId);
        return sections.stream().map(sectionMapper::toDto).toList();
    }

    /** Get active sections only. */
    @Transactional(readOnly = true)
    public List<SectionDto> getActiveSections() {
        String restaurantId = TenantContext.getRestaurantId();
        List<Section> sections =
                sectionRepository.findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(
                        restaurantId);
        return sections.stream().map(sectionMapper::toDto).toList();
    }

    /** Get a section by ID. */
    @Transactional(readOnly = true)
    public SectionDto getSectionById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        Section section =
                sectionRepository
                        .findById(id)
                        .orElseThrow(() -> new NotFoundException("Section", id));

        if (!section.getRestaurantId().equals(restaurantId)) {
            throw new NotFoundException("Section", id);
        }

        return sectionMapper.toDto(section);
    }

    /** Update a section. */
    @Transactional
    public SectionDto updateSection(String id, UpdateSectionRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        Section section =
                sectionRepository
                        .findById(id)
                        .orElseThrow(() -> new NotFoundException("Section", id));

        if (!section.getRestaurantId().equals(restaurantId)) {
            throw new NotFoundException("Section", id);
        }

        if (request.name() != null) {
            section.setName(request.name());
        }
        if (request.description() != null) {
            section.setDescription(request.description());
        }
        if (request.displayOrder() != null) {
            section.setDisplayOrder(request.displayOrder());
        }
        if (request.isActive() != null) {
            section.setActive(request.isActive());
        }

        Section updated = sectionRepository.save(section);
        return sectionMapper.toDto(updated);
    }

    /** Delete a section. */
    @Transactional
    public void deleteSection(String id) {
        String restaurantId = TenantContext.getRestaurantId();

        Section section =
                sectionRepository
                        .findById(id)
                        .orElseThrow(() -> new NotFoundException("Section", id));

        if (!section.getRestaurantId().equals(restaurantId)) {
            throw new NotFoundException("Section", id);
        }

        // Check if section has categories
        // This would require a CategoryRepository check, skipping for now
        // In production, you'd check: if (categoryRepository.existsBySectionId(id)) throw
        // ConflictException

        sectionRepository.delete(section);
    }
}
