package com.logiflow.shared.exception;

import java.util.List;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public static ProductNotFoundException forId(String id) {
        return new ProductNotFoundException("Product not found with id: " + id);
    }

    public static ProductNotFoundException forSkus(List<String> skus) {
        return new ProductNotFoundException("Products not found for SKUs: " + skus);
    }
}
