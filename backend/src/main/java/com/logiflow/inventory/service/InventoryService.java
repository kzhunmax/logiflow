package com.logiflow.inventory.service;

import com.logiflow.inventory.model.Inventory;
import com.logiflow.inventory.repository.InventoryRepository;
import com.logiflow.shared.exception.InsufficientStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public void addStock(String sku, Integer amount) {
        inventoryRepository.findBySku(sku).ifPresentOrElse(
                (inventory -> {
                    inventory.setQuantity(inventory.getQuantity() + amount);
                    inventoryRepository.save(inventory);
                }),
                () -> {
                    var inventory = new Inventory();
                    inventory.setSku(sku);
                    inventory.setQuantity(amount);
                    inventoryRepository.save(inventory);
                }

        );
    }

    public void reserveStock(String sku, Integer amount) {
        inventoryRepository.findBySku(sku).ifPresent(inventory -> {
            if (inventory.getQuantity() - inventory.getReserved() >= amount) {
                inventory.setReserved(inventory.getReserved() + amount);
                inventoryRepository.save(inventory);
            } else {
                throw new InsufficientStockException("Insufficient stock available to reserve");
            }
        });
    }
}
