package com.logiflow.catalog.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;

@Document(collection = "products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Product entity representing an item in the catalog")
public class Product {

    @Id
    @Schema(description = "Unique product identifier", example = "507f1f77bcf86cd799439011", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "Product name", example = "Wireless Mouse")
    private String name;

    @Schema(description = "Stock Keeping Unit - unique product identifier for inventory", example = "WM-001")
    private String sku;

    @Schema(description = "Product price", example = "29.99")
    private BigDecimal price;

    @Schema(description = "Additional product attributes as key-value pairs", example = "{\"color\": \"black\", \"weight\": \"100g\"}")
    private Map<String, Object> attributes;

    @Builder.Default
    @Schema(description = "Whether the product is active (false indicates soft-deleted)", example = "true")
    private Boolean active = true;
}
