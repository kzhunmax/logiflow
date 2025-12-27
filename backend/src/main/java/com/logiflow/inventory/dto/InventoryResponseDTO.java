package com.logiflow.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing inventory information for a product")
public record InventoryResponseDTO(
        @Schema(description = "Stock Keeping Unit - unique product identifier", example = "WM-001")
        String sku,

        @Schema(description = "Available quantity in stock (total quantity minus reserved)", example = "150")
        Integer availableQuantity
) {
}
