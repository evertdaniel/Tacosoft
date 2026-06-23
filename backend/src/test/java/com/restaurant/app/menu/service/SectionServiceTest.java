package com.restaurant.app.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.menu.dto.CreateSectionRequest;
import com.restaurant.app.menu.dto.SectionDto;
import com.restaurant.app.menu.dto.UpdateSectionRequest;
import com.restaurant.app.menu.mapper.SectionMapper;
import com.restaurant.app.menu.model.Section;
import com.restaurant.app.menu.repository.SectionRepository;
import com.restaurant.app.security.TenantContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link SectionService}. SPEC-MENU-001. */
@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock private SectionRepository sectionRepository;

    @Mock private SectionMapper sectionMapper;

    @InjectMocks private SectionService sectionService;

    private final String restaurantId = "restaurant-1";
    private final String otherRestaurantId = "restaurant-2";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createSection_ValidRequest_SavesAndReturnsDto() {
        CreateSectionRequest request = new CreateSectionRequest("Lunch", "Midday menu", 1, true);

        when(sectionRepository.save(any(Section.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sectionMapper.toDto(any(Section.class))).thenReturn(sectionDto("section-1"));

        SectionDto result = sectionService.createSection(request);

        assertThat(result.id()).isEqualTo("section-1");
        verify(sectionRepository).save(argThat(section -> section.getName().equals("Lunch")));
    }

    @Test
    void getAllSections_ReturnsSectionsOrderedByDisplayOrder() {
        Section section = sectionEntity("section-1", "Dinner", 2);
        when(sectionRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId))
                .thenReturn(List.of(section));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto("section-1"));

        List<SectionDto> result = sectionService.getAllSections();

        assertThat(result).hasSize(1);
        verify(sectionRepository).findByRestaurantIdOrderByDisplayOrderAsc(restaurantId);
    }

    @Test
    void getActiveSections_ReturnsOnlyActiveSections() {
        Section active = sectionEntity("section-1", "Active", 1);
        when(sectionRepository.findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(
                        restaurantId))
                .thenReturn(List.of(active));
        when(sectionMapper.toDto(active)).thenReturn(sectionDto("section-1"));

        List<SectionDto> result = sectionService.getActiveSections();

        assertThat(result).hasSize(1);
        verify(sectionRepository)
                .findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId);
    }

    @Test
    void getSectionById_ExistingSection_ReturnsDto() {
        Section section = sectionEntity("section-1", "Lunch", 1);
        when(sectionRepository.findById("section-1")).thenReturn(Optional.of(section));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto("section-1"));

        SectionDto result = sectionService.getSectionById("section-1");

        assertThat(result.id()).isEqualTo("section-1");
    }

    @Test
    void getSectionById_SectionFromOtherRestaurant_ThrowsNotFoundException() {
        Section section = sectionEntity("section-1", "Lunch", 1);
        section.setRestaurantId(otherRestaurantId);
        when(sectionRepository.findById("section-1")).thenReturn(Optional.of(section));

        assertThatThrownBy(() -> sectionService.getSectionById("section-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getSectionById_SectionNotFound_ThrowsNotFoundException() {
        when(sectionRepository.findById("section-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.getSectionById("section-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateSection_ExistingSection_UpdatesAllFields() {
        Section section = sectionEntity("section-1", "Lunch", 1);
        UpdateSectionRequest request = new UpdateSectionRequest("Dinner", "Evening menu", 2, false);

        when(sectionRepository.findById("section-1")).thenReturn(Optional.of(section));
        when(sectionRepository.save(any(Section.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto("section-1"));

        SectionDto result = sectionService.updateSection("section-1", request);

        assertThat(section.getName()).isEqualTo("Dinner");
        assertThat(section.getDescription()).isEqualTo("Evening menu");
        assertThat(section.getDisplayOrder()).isEqualTo(2);
        assertThat(section.isActive()).isFalse();
        assertThat(result.id()).isEqualTo("section-1");
    }

    @Test
    void updateSection_PartialFields_UpdatesOnlyProvidedFields() {
        Section section = sectionEntity("section-1", "Lunch", 1);
        UpdateSectionRequest request = new UpdateSectionRequest(null, null, 5, null);

        when(sectionRepository.findById("section-1")).thenReturn(Optional.of(section));
        when(sectionRepository.save(any(Section.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto("section-1"));

        sectionService.updateSection("section-1", request);

        assertThat(section.getName()).isEqualTo("Lunch");
        assertThat(section.getDisplayOrder()).isEqualTo(5);
    }

    @Test
    void updateSection_SectionNotFound_ThrowsNotFoundException() {
        UpdateSectionRequest request = new UpdateSectionRequest("Dinner", null, 1, true);
        when(sectionRepository.findById("section-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.updateSection("section-1", request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateSection_SectionFromOtherRestaurant_ThrowsNotFoundException() {
        Section section = sectionEntity("section-1", "Lunch", 1);
        section.setRestaurantId(otherRestaurantId);
        UpdateSectionRequest request = new UpdateSectionRequest("Dinner", null, 1, true);

        when(sectionRepository.findById("section-1")).thenReturn(Optional.of(section));

        assertThatThrownBy(() -> sectionService.updateSection("section-1", request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteSection_ExistingSection_DeletesSuccessfully() {
        Section section = sectionEntity("section-1", "Lunch", 1);
        when(sectionRepository.findById("section-1")).thenReturn(Optional.of(section));

        sectionService.deleteSection("section-1");

        verify(sectionRepository).delete(section);
    }

    @Test
    void deleteSection_SectionNotFound_ThrowsNotFoundException() {
        when(sectionRepository.findById("section-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.deleteSection("section-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteSection_SectionFromOtherRestaurant_ThrowsNotFoundException() {
        Section section = sectionEntity("section-1", "Lunch", 1);
        section.setRestaurantId(otherRestaurantId);
        when(sectionRepository.findById("section-1")).thenReturn(Optional.of(section));

        assertThatThrownBy(() -> sectionService.deleteSection("section-1"))
                .isInstanceOf(NotFoundException.class);
        verify(sectionRepository, never()).delete(any(Section.class));
    }

    private Section sectionEntity(String id, String name, int displayOrder) {
        return Section.builder()
                .id(id)
                .restaurantId(restaurantId)
                .name(name)
                .description("Description")
                .displayOrder(displayOrder)
                .isActive(true)
                .build();
    }

    private SectionDto sectionDto(String id) {
        return new SectionDto(id, restaurantId, "Section", "Description", 1, true);
    }
}
