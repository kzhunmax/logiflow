package com.logiflow.catalog.repository;

import com.logiflow.catalog.model.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<@NonNull Product, @NonNull String> {
}
