package com.logiflow.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockAdjustmentDTO(
        @NotBlank String sku,
        @NotNull @Positive Integer adjustmentQuantity,
        @NotNull AdjustmentType type
) {

    public enum AdjustmentType {
        ADD, REMOVE
    }
}
