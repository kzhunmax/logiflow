package com.logiflow.catalog.repository;

import com.logiflow.catalog.model.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<@NonNull Product, @NonNull String> {
    Page<Product> findByActiveTrue(Pageable pageable);
    Optional<Product> findBySkuAndActiveTrue(String sku);
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrueOrSkuContainingIgnoreCaseAndActiveTrue(String name, String sku, Pageable pageable);
}
