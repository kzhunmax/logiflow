package com.logiflow.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;

@Document(collection = "products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Product {

    @Id
    private String id;

    private String name;
    private String sku;
    private BigDecimal price;

    private Map<String, Object> attributes;
}
