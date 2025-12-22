package com.logiflow.inventory.repository;

import com.logiflow.inventory.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InventoryRepository extends MongoRepository<Inventory, String> {

    Optional<Inventory> findBySku(String sku);
}
