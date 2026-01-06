package com.logiflow.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Individual item within an order response")
public record OrderItemResponseDTO(
        @Schema(description = "Stock Keeping Unit identifier", example = "SKU-001")
        String sku,

        @Schema(description = "Quantity ordered", example = "2")
        Integer quantity,

        @Schema(description = "Price of the product at the time the order was placed", example = "29.99")
        BigDecimal priceAtTimeOfOrder
) {
}

