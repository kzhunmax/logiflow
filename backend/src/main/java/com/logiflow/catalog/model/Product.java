package com.logiflow.catalog.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;

@Document(collection = "products")
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
