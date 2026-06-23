package com.restaurant.app.table.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.restaurant.app.common.ConflictException;
import com.restaurant.app.common.NotFoundException;
import com.restaurant.app.security.TenantContext;
import com.restaurant.app.table.dto.CreateTableRequest;
import com.restaurant.app.table.dto.TableDto;
import com.restaurant.app.table.dto.UpdateTableRequest;
import com.restaurant.app.table.dto.UpdateTableStatusRequest;
import com.restaurant.app.table.mapper.TableMapper;
import com.restaurant.app.table.model.RestaurantTable;
import com.restaurant.app.table.repository.TableRepository;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

/** Unit tests for {@link TableService}. */
@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock private TableRepository tableRepository;

    @Mock private TableMapper tableMapper;

    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private TableService tableService;

    private final String restaurantId = "restaurant-1";
    private final String tableId = "table-1";

    @BeforeEach
    void setUp() {
        TenantContext.setRestaurantId(restaurantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAllTables_ReturnsTablesForRestaurant() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        when(tableRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(table));
        when(tableMapper.toDto(table)).thenReturn(tableDto(tableId, 1));

        List<TableDto> result = tableService.getAllTables();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(tableId);
        verify(tableRepository).findByRestaurantId(restaurantId);
    }

    @Test
    void getActiveTables_ReturnsOnlyActiveTables() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        table.setActive(true);
        when(tableRepository.findByRestaurantIdAndIsActive(restaurantId, true))
                .thenReturn(List.of(table));
        when(tableMapper.toDto(table)).thenReturn(tableDto(tableId, 1));

        List<TableDto> result = tableService.getActiveTables();

        assertThat(result).hasSize(1);
        verify(tableRepository).findByRestaurantIdAndIsActive(restaurantId, true);
    }

    @Test
    void getAvailableTables_ReturnsOnlyAvailableTables() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        when(tableRepository.findByRestaurantIdAndStatus(restaurantId, "AVAILABLE"))
                .thenReturn(List.of(table));
        when(tableMapper.toDto(table)).thenReturn(tableDto(tableId, 1));

        List<TableDto> result = tableService.getAvailableTables();

        assertThat(result).hasSize(1);
        verify(tableRepository).findByRestaurantIdAndStatus(restaurantId, "AVAILABLE");
    }

    @Test
    void getTableById_ExistingTable_ReturnsDto() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));
        when(tableMapper.toDto(table)).thenReturn(tableDto(tableId, 1));

        TableDto result = tableService.getTableById(tableId);

        assertThat(result.getId()).isEqualTo(tableId);
    }

    @Test
    void getTableById_TableNotFound_ThrowsNotFoundException() {
        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.getTableById(tableId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Table");
    }

    @Test
    void createTable_ValidRequest_SavesAndBroadcasts() {
        CreateTableRequest request = createRequest();
        when(tableRepository.existsByRestaurantIdAndNum(restaurantId, 1)).thenReturn(false);
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tableMapper.toDto(any(RestaurantTable.class))).thenReturn(tableDto(tableId, 1));

        TableDto result = tableService.createTable(request);

        assertThat(result.getId()).isEqualTo(tableId);
        assertThat(result.getStatus()).isEqualTo("AVAILABLE");
        verify(tableRepository).save(argThat(t -> t.getNum().equals(1) && t.getSeats().equals(4)));
        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/restaurant/" + restaurantId + "/tables"), any(TableDto.class));
    }

    @Test
    void createTable_DuplicateNumber_ThrowsConflictException() {
        CreateTableRequest request = createRequest();
        when(tableRepository.existsByRestaurantIdAndNum(restaurantId, 1)).thenReturn(true);

        assertThatThrownBy(() -> tableService.createTable(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
        verify(tableRepository, never()).save(any(RestaurantTable.class));
    }

    @Test
    void updateTable_ExistingTable_UpdatesAllFields() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        UpdateTableRequest request = updateRequest();
        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));
        when(tableRepository.existsByRestaurantIdAndNum(restaurantId, 2)).thenReturn(false);
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tableMapper.toDto(any(RestaurantTable.class))).thenReturn(tableDto(tableId, 2));

        TableDto result = tableService.updateTable(tableId, request);

        assertThat(table.getNum()).isEqualTo(2);
        assertThat(table.getSeats()).isEqualTo(6);
        assertThat(table.getPosX()).isEqualTo(10);
        assertThat(table.getPosY()).isEqualTo(20);
        assertThat(table.isActive()).isFalse();
        assertThat(result.getId()).isEqualTo(tableId);
        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/restaurant/" + restaurantId + "/tables"), any(TableDto.class));
    }

    @Test
    void updateTable_SameNumber_DoesNotCheckConflict() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        UpdateTableRequest request = new UpdateTableRequest();
        request.setNum(1);

        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tableMapper.toDto(any(RestaurantTable.class))).thenReturn(tableDto(tableId, 1));

        tableService.updateTable(tableId, request);

        verify(tableRepository, never()).existsByRestaurantIdAndNum(any(), any());
    }

    @Test
    void updateTable_NewNumberConflict_ThrowsConflictException() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        UpdateTableRequest request = new UpdateTableRequest();
        request.setNum(2);

        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));
        when(tableRepository.existsByRestaurantIdAndNum(restaurantId, 2)).thenReturn(true);

        assertThatThrownBy(() -> tableService.updateTable(tableId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateTable_TableNotFound_ThrowsNotFoundException() {
        UpdateTableRequest request = new UpdateTableRequest();
        request.setNum(2);
        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.updateTable(tableId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Table");
    }

    @Test
    void updateTableStatus_ValidTransition_SavesAndBroadcasts() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        UpdateTableStatusRequest request = new UpdateTableStatusRequest();
        request.setStatus("OCCUPIED");

        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tableMapper.toDto(any(RestaurantTable.class))).thenReturn(tableDto(tableId, 1));

        TableDto result = tableService.updateTableStatus(tableId, request);

        assertThat(table.getStatus()).isEqualTo("OCCUPIED");
        assertThat(result.getId()).isEqualTo(tableId);
        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/restaurant/" + restaurantId + "/tables"), any(TableDto.class));
    }

    @Test
    void updateTableStatus_InvalidTransition_ThrowsConflictException() {
        RestaurantTable table = tableEntity(tableId, 1, "OCCUPIED");
        UpdateTableStatusRequest request = new UpdateTableStatusRequest();
        request.setStatus("CLEANING");

        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));

        assertThatThrownBy(() -> tableService.updateTableStatus(tableId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateTableStatus_SameStatus_Allowed() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        UpdateTableStatusRequest request = new UpdateTableStatusRequest();
        request.setStatus("AVAILABLE");

        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tableMapper.toDto(any(RestaurantTable.class))).thenReturn(tableDto(tableId, 1));

        TableDto result = tableService.updateTableStatus(tableId, request);

        assertThat(table.getStatus()).isEqualTo("AVAILABLE");
        assertThat(result.getId()).isEqualTo(tableId);
    }

    @Test
    void deleteTable_NonOccupiedTable_DeletesAndBroadcasts() {
        RestaurantTable table = tableEntity(tableId, 1, "AVAILABLE");
        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));
        when(tableMapper.toDto(table)).thenReturn(tableDto(tableId, 1));

        tableService.deleteTable(tableId);

        verify(tableRepository).delete(table);
        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/restaurant/" + restaurantId + "/tables"), any(TableDto.class));
    }

    @Test
    void deleteTable_OccupiedTable_ThrowsConflictException() {
        RestaurantTable table = tableEntity(tableId, 1, "OCCUPIED");
        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.of(table));

        assertThatThrownBy(() -> tableService.deleteTable(tableId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot delete occupied table");
        verify(tableRepository, never()).delete(any(RestaurantTable.class));
    }

    @Test
    void deleteTable_TableNotFound_ThrowsNotFoundException() {
        when(tableRepository.findByIdAndRestaurantId(tableId, restaurantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.deleteTable(tableId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Table");
    }

    private CreateTableRequest createRequest() {
        CreateTableRequest request = new CreateTableRequest();
        request.setNum(1);
        request.setSeats(4);
        request.setPosX(5);
        request.setPosY(10);
        return request;
    }

    private UpdateTableRequest updateRequest() {
        UpdateTableRequest request = new UpdateTableRequest();
        request.setNum(2);
        request.setSeats(6);
        request.setPosX(10);
        request.setPosY(20);
        request.setActive(false);
        return request;
    }

    private RestaurantTable tableEntity(String id, int num, String status) {
        return RestaurantTable.builder()
                .id(id)
                .restaurantId(restaurantId)
                .num(num)
                .seats(4)
                .status(status)
                .posX(0)
                .posY(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private TableDto tableDto(String id, int num) {
        TableDto dto = new TableDto();
        dto.setId(id);
        dto.setNum(num);
        dto.setSeats(4);
        dto.setStatus("AVAILABLE");
        dto.setPosX(0);
        dto.setPosY(0);
        dto.setActive(true);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}
