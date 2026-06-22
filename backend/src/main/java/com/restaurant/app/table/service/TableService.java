package com.restaurant.app.table.service;

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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for table operations with status transitions. */
@Service
public class TableService {

    private final TableRepository tableRepository;
    private final TableMapper tableMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // Valid status transitions
    private static final Set<String> AVAILABLE_TRANSITIONS =
            Set.of("OCCUPIED", "RESERVED", "CLEANING");
    private static final Set<String> OCCUPIED_TRANSITIONS = Set.of("AVAILABLE");
    private static final Set<String> RESERVED_TRANSITIONS = Set.of("AVAILABLE", "OCCUPIED");
    private static final Set<String> CLEANING_TRANSITIONS = Set.of("AVAILABLE");

    public TableService(
            TableRepository tableRepository,
            TableMapper tableMapper,
            SimpMessagingTemplate messagingTemplate) {
        this.tableRepository = tableRepository;
        this.tableMapper = tableMapper;
        this.messagingTemplate = messagingTemplate;
    }

    /** Get all tables for current restaurant. */
    @Transactional(readOnly = true)
    public List<TableDto> getAllTables() {
        String restaurantId = TenantContext.getRestaurantId();
        return tableRepository.findByRestaurantId(restaurantId).stream()
                .map(tableMapper::toDto)
                .toList();
    }

    /** Get active tables only. */
    @Transactional(readOnly = true)
    public List<TableDto> getActiveTables() {
        String restaurantId = TenantContext.getRestaurantId();
        return tableRepository.findByRestaurantIdAndIsActive(restaurantId, true).stream()
                .map(tableMapper::toDto)
                .toList();
    }

    /** Get available tables. */
    @Transactional(readOnly = true)
    public List<TableDto> getAvailableTables() {
        String restaurantId = TenantContext.getRestaurantId();
        return tableRepository.findByRestaurantIdAndStatus(restaurantId, "AVAILABLE").stream()
                .map(tableMapper::toDto)
                .toList();
    }

    /** Get a table by ID. */
    @Transactional(readOnly = true)
    public TableDto getTableById(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        RestaurantTable table =
                tableRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Table", id));
        return tableMapper.toDto(table);
    }

    /** Create a new table. */
    @Transactional
    public TableDto createTable(CreateTableRequest request) {
        String restaurantId = TenantContext.getRestaurantId();

        // Check if table number already exists
        if (tableRepository.existsByRestaurantIdAndNum(restaurantId, request.getNum())) {
            throw new ConflictException("Table number " + request.getNum() + " already exists");
        }

        RestaurantTable table =
                RestaurantTable.builder()
                        .id(UUID.randomUUID().toString())
                        .restaurantId(restaurantId)
                        .num(request.getNum())
                        .seats(request.getSeats())
                        .status("AVAILABLE")
                        .posX(request.getPosX())
                        .posY(request.getPosY())
                        .isActive(true)
                        .build();

        table = tableRepository.save(table);

        // Broadcast table creation
        broadcastTableChange(table);

        return tableMapper.toDto(table);
    }

    /** Update a table. */
    @Transactional
    public TableDto updateTable(String id, UpdateTableRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        RestaurantTable table =
                tableRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Table", id));

        if (request.getNum() != null) {
            // Check if new number conflicts with another table
            if (!table.getNum().equals(request.getNum())
                    && tableRepository.existsByRestaurantIdAndNum(restaurantId, request.getNum())) {
                throw new ConflictException("Table number " + request.getNum() + " already exists");
            }
            table.setNum(request.getNum());
        }

        if (request.getSeats() != null) {
            table.setSeats(request.getSeats());
        }

        if (request.getPosX() != null) {
            table.setPosX(request.getPosX());
        }

        if (request.getPosY() != null) {
            table.setPosY(request.getPosY());
        }

        if (request.isActive() != null) {
            table.setActive(request.isActive());
        }

        table = tableRepository.save(table);

        // Broadcast table update
        broadcastTableChange(table);

        return tableMapper.toDto(table);
    }

    /** Update table status with transition validation. */
    @Transactional
    public TableDto updateTableStatus(String id, UpdateTableStatusRequest request) {
        String restaurantId = TenantContext.getRestaurantId();
        RestaurantTable table =
                tableRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Table", id));

        String currentStatus = table.getStatus();
        String newStatus = request.getStatus();

        // Validate status transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new ConflictException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        table.setStatus(newStatus);
        table = tableRepository.save(table);

        // Broadcast table status change
        broadcastTableChange(table);

        return tableMapper.toDto(table);
    }

    /** Delete a table. */
    @Transactional
    public void deleteTable(String id) {
        String restaurantId = TenantContext.getRestaurantId();
        RestaurantTable table =
                tableRepository
                        .findByIdAndRestaurantId(id, restaurantId)
                        .orElseThrow(() -> new NotFoundException("Table", id));

        // Prevent deletion if table is occupied
        if ("OCCUPIED".equals(table.getStatus())) {
            throw new ConflictException("Cannot delete occupied table");
        }

        tableRepository.delete(table);

        // Broadcast table deletion
        broadcastTableChange(table);
    }

    /** Validate if a status transition is allowed. */
    private boolean isValidTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return true; // No change
        }

        return switch (currentStatus) {
            case "AVAILABLE" -> AVAILABLE_TRANSITIONS.contains(newStatus);
            case "OCCUPIED" -> OCCUPIED_TRANSITIONS.contains(newStatus);
            case "RESERVED" -> RESERVED_TRANSITIONS.contains(newStatus);
            case "CLEANING" -> CLEANING_TRANSITIONS.contains(newStatus);
            default -> false;
        };
    }

    /** Broadcast table change to WebSocket topic. */
    private void broadcastTableChange(RestaurantTable table) {
        String restaurantId = TenantContext.getRestaurantId();
        messagingTemplate.convertAndSend(
                "/topic/restaurant/" + restaurantId + "/tables", tableMapper.toDto(table));
    }
}
