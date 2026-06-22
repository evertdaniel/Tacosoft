package com.restaurant.app.table.mapper;

import com.restaurant.app.table.dto.TableDto;
import com.restaurant.app.table.model.RestaurantTable;
import org.springframework.stereotype.Component;

/** Mapper for Table entity and DTOs. */
@Component
public class TableMapper {

    public TableDto toDto(RestaurantTable table) {
        return new TableDto(
                table.getId(),
                table.getNum(),
                table.getSeats(),
                table.getStatus(),
                table.getPosX(),
                table.getPosY(),
                table.isActive(),
                table.getCreatedAt(),
                table.getUpdatedAt());
    }
}
