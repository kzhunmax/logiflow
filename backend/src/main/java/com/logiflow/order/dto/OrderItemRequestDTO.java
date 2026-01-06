package com.logiflow.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Individual item within an order request")
public record OrderItemRequestDTO(
        @Schema(description = "Stock Keeping Unit identifier", example = "SKU-001")
        @NotBlank(message = "SKU is required")
        String sku,

        @Schema(description = "Quantity to order", example = "2", minimum = "1")
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}

