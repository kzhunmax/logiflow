package com.logiflow.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Request payload for creating or updating a product")
public record ProductRequestDTO(
        @Schema(description = "Product name", example = "Wireless Mouse", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Product name must not be blank")
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
        String name,

        @Schema(description = "Stock Keeping Unit - unique product identifier", example = "WM-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SKU must not be blank")
        @Size(min = 2, max = 150, message = "SKU must be between 2 and 150 characters")
        String sku,

        @Schema(description = "Product price", example = "29.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @Schema(description = "Additional product attributes as key-value pairs", example = "{\"color\": \"black\", \"weight\": \"100g\"}")
        Map<String, Object> attributes
) {
}
