package com.logiflow.inventory.dto;

public record InventoryResponseDTO(
        String sku,
        Integer availableQuantity
) {
}
