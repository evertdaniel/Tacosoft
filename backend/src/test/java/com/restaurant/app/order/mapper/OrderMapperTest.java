package com.restaurant.app.order.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.app.menu.model.Product;
import com.restaurant.app.menu.model.ProductOption;
import com.restaurant.app.order.dto.OrderDetailDto;
import com.restaurant.app.order.dto.OrderDto;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.model.OrderDetail;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link OrderMapper}. */
class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void toDto_MapsOrderAndDetails() {
        Order order =
                Order.builder()
                        .id("order-1")
                        .restaurantId("restaurant-1")
                        .num(1)
                        .type("DINE_IN")
                        .status("OPEN")
                        .total(BigDecimal.valueOf(20.00))
                        .people(2)
                        .tableId("table-1")
                        .clientId("client-1")
                        .build();
        order.setCreatedAt(LocalDateTime.of(2026, 6, 22, 10, 0));
        order.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 11, 0));

        OrderDetailDto detailDto = new OrderDetailDto();
        detailDto.setId("detail-1");

        OrderDto dto = mapper.toDto(order, List.of(detailDto));

        assertThat(dto.getId()).isEqualTo("order-1");
        assertThat(dto.getNum()).isEqualTo(1);
        assertThat(dto.getType()).isEqualTo("DINE_IN");
        assertThat(dto.getStatus()).isEqualTo("OPEN");
        assertThat(dto.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
        assertThat(dto.getPeople()).isEqualTo(2);
        assertThat(dto.getTableId()).isEqualTo("table-1");
        assertThat(dto.getClientId()).isEqualTo("client-1");
        assertThat(dto.getDetails()).containsExactly(detailDto);
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 0));
    }

    @Test
    void toDetailDto_MapsDetailWithProductAndOption() {
        Product product = Product.builder().id("product-1").name("Burger").build();
        ProductOption option =
                ProductOption.builder()
                        .id("option-1")
                        .name("Extra cheese")
                        .priceAdjustment(BigDecimal.valueOf(0.50))
                        .build();
        OrderDetail detail =
                OrderDetail.builder()
                        .id("detail-1")
                        .restaurantId("restaurant-1")
                        .orderId("order-1")
                        .productId("product-1")
                        .quantity(2)
                        .price(BigDecimal.valueOf(9.99))
                        .amount(BigDecimal.valueOf(19.98))
                        .status("PENDING")
                        .notes("No onions")
                        .productOptionId("option-1")
                        .build();
        detail.setProduct(product);
        detail.setProductOption(option);
        detail.setCreatedAt(LocalDateTime.of(2026, 6, 22, 10, 0));
        detail.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 11, 0));

        OrderDetailDto dto = mapper.toDetailDto(detail);

        assertThat(dto.getId()).isEqualTo("detail-1");
        assertThat(dto.getProductName()).isEqualTo("Burger");
        assertThat(dto.getProductOptionName()).isEqualTo("Extra cheese");
        assertThat(dto.getPriceAdjustment()).isEqualByComparingTo(BigDecimal.valueOf(0.50));
        assertThat(dto.getQuantity()).isEqualTo(2);
        assertThat(dto.getNotes()).isEqualTo("No onions");
    }

    @Test
    void toDetailDto_MapsDetailWithoutProductAndOption() {
        OrderDetail detail =
                OrderDetail.builder()
                        .id("detail-1")
                        .restaurantId("restaurant-1")
                        .orderId("order-1")
                        .productId("product-1")
                        .quantity(1)
                        .price(BigDecimal.valueOf(5.00))
                        .amount(BigDecimal.valueOf(5.00))
                        .status("PENDING")
                        .build();

        OrderDetailDto dto = mapper.toDetailDto(detail);

        assertThat(dto.getProductName()).isNull();
        assertThat(dto.getProductOptionName()).isNull();
        assertThat(dto.getPriceAdjustment()).isNull();
    }
}
