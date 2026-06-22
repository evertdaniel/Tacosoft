package com.restaurant.app.table.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.table.dto.TableDto;
import com.restaurant.app.table.model.RestaurantTable;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TableMapper}. */
class TableMapperTest {

    private final TableMapper mapper = new TableMapper();

    @Test
    void toDto_MapsTableEntityToDto() {
        RestaurantTable table =
                RestaurantTable.builder()
                        .id("table-1")
                        .num(1)
                        .seats(4)
                        .status("AVAILABLE")
                        .posX(10)
                        .posY(20)
                        .isActive(true)
                        .createdAt(LocalDateTime.of(2026, 6, 22, 10, 0))
                        .updatedAt(LocalDateTime.of(2026, 6, 22, 11, 0))
                        .build();

        TableDto dto = mapper.toDto(table);

        assertThat(dto.getId()).isEqualTo("table-1");
        assertThat(dto.getNum()).isEqualTo(1);
        assertThat(dto.getSeats()).isEqualTo(4);
        assertThat(dto.getStatus()).isEqualTo("AVAILABLE");
        assertThat(dto.getPosX()).isEqualTo(10);
        assertThat(dto.getPosY()).isEqualTo(20);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 0));
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 11, 0));
    }
}
