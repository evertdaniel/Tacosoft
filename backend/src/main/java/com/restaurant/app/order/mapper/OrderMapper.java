package com.restaurant.app.order.mapper;

import com.restaurant.app.order.dto.OrderDetailDto;
import com.restaurant.app.order.dto.OrderDto;
import com.restaurant.app.order.model.Order;
import com.restaurant.app.order.model.OrderDetail;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/** Mapper for Order entity and DTOs. */
@Component
public class OrderMapper {

    public OrderDto toDto(Order order, List<OrderDetailDto> details) {
        return new OrderDto(
                order.getId(),
                order.getNum(),
                order.getType(),
                order.getStatus(),
                order.getTotal(),
                order.getPeople(),
                order.getTableId(),
                order.getClientId(),
                details,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    public OrderDetailDto toDetailDto(OrderDetail detail) {
        String productName = detail.getProduct() != null ? detail.getProduct().getName() : null;
        String optionName =
                detail.getProductOption() != null ? detail.getProductOption().getName() : null;
        BigDecimal priceAdjustment =
                detail.getProductOption() != null
                        ? detail.getProductOption().getPriceAdjustment()
                        : null;

        return new OrderDetailDto(
                detail.getId(),
                detail.getOrderId(),
                detail.getProductId(),
                productName,
                detail.getQuantity(),
                detail.getPrice(),
                detail.getAmount(),
                detail.getStatus(),
                detail.getNotes(),
                detail.getProductOptionId(),
                optionName,
                priceAdjustment,
                detail.getCreatedAt(),
                detail.getUpdatedAt());
    }
}
