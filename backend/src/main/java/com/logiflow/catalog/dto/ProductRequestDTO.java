package com.logiflow.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public record ProductRequestDTO(
        @NotBlank(message = "Product name must not be blank")
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "SKU must not be blank")
        @Size(min = 2, max = 150, message = "SKU must be between 2 and 150 characters")
        String sku,

        @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        Map<String, Object> attributes
) {
}
