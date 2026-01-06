package com.logiflow.order.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        String sku,
        Integer quantity,
        BigDecimal priceAtTimeOfOrder
) {
}

