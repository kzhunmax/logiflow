package com.logiflow.catalog.repository;

import com.logiflow.catalog.model.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<@NonNull Product, @NonNull String> {
    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseAndActiveTrue(String name, String sku, Pageable pageable);
}
