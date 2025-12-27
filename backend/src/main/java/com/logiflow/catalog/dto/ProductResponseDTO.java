package com.logiflow.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Request payload for creating or updating a product")
public record ProductResponseDTO(

        @Schema(description = "Unique product identifier", example = "507f1f77bcf86cd799439011", accessMode = Schema.AccessMode.READ_ONLY)
        String id,

        @Schema(description = "Product name", example = "Wireless Mouse", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(description = "Stock Keeping Unit - unique product identifier for inventory", example = "WM-001", requiredMode = Schema.RequiredMode.REQUIRED)
        String sku,

        @Schema(description = "Product price", example = "29.99", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal price,

        @Schema(description = "Additional product attributes as key-value pairs", example = "{\"color\": \"black\", \"weight\": \"100g\"}")
        Map<String, Object> attributes,

        @Schema(description = "Whether the product is active (false indicates soft-deleted)", example = "true")
        Boolean active
) {
}
