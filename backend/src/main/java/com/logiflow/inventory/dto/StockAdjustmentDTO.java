package com.logiflow.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request payload for adjusting stock levels")
public record StockAdjustmentDTO(
        @Schema(description = "Stock Keeping Unit - unique product identifier", example = "WM-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String sku,

        @Schema(description = "Quantity to add or remove from stock", example = "10", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Integer adjustmentQuantity,

        @Schema(description = "Type of stock adjustment", example = "ADD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull AdjustmentType type
) {

    @Schema(description = "Type of stock adjustment operation")
    public enum AdjustmentType {
        @Schema(description = "Add stock to inventory")
        ADD,
        @Schema(description = "Remove/reserve stock from inventory")
        REMOVE
    }
}
