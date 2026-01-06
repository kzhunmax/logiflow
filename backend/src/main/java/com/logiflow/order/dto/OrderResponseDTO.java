package com.logiflow.order.dto;

import com.logiflow.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response payload representing an order")
public record OrderResponseDTO(
        @Schema(description = "Unique order identifier", example = "1")
        Long id,

        @Schema(description = "Name of the customer", example = "John Doe")
        String customerName,

        @Schema(description = "Current order status")
        OrderStatus status,

        @Schema(description = "Timestamp when the order was created", example = "2026-01-06T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "List of items in the order")
        List<OrderItemResponseDTO> items
) {
}
