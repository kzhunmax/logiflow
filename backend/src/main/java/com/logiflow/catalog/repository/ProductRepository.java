package com.logiflow.catalog.repository;

import com.logiflow.catalog.model.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<@NonNull Product, @NonNull String> {
    List<Product> findByActiveTrue();
}
