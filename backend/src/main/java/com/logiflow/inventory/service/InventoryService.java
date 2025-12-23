package com.logiflow.inventory.service;

import com.logiflow.inventory.dto.InventoryResponseDTO;
import com.logiflow.inventory.model.Inventory;
import com.logiflow.inventory.repository.InventoryRepository;
import com.logiflow.shared.exception.InsufficientStockException;
import com.logiflow.shared.exception.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void addStock(String sku, Integer amount) {
        try {
            attemptAddStock(sku, amount);
        } catch (DataIntegrityViolationException e) {
            attemptAddStock(sku, amount);
        }
    }

    private void attemptAddStock(String sku, Integer amount) {
        Optional<Inventory> existing = inventoryRepository.findBySku(sku);
        if (existing.isPresent()) {
            Inventory inventory = existing.get();
            inventory.setQuantity(inventory.getQuantity() + amount);
            inventoryRepository.save(inventory);
        } else {
            Inventory newInventory = Inventory.builder()
                    .sku(sku)
                    .quantity(amount)
                    .reserved(0)
                    .build();
            inventoryRepository.save(newInventory);
        }
    }

    @Transactional
    public void reserveStock(String sku, Integer amount) {
        Inventory inventory = inventoryRepository.findBySkuForUpdate(sku).orElseThrow(() -> new InventoryNotFoundException(sku));
        if (inventory.getQuantity() - inventory.getReserved() >= amount) {
            inventory.setReserved(inventory.getReserved() + amount);
            inventoryRepository.save(inventory);
        } else {
            throw new InsufficientStockException("Insufficient stock available to reserve");
        }
    }

    @Transactional(readOnly = true)
    public InventoryResponseDTO getAvailableInventory(String sku) {
        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));
        int available = inventory.getQuantity() - inventory.getReserved();
        return new InventoryResponseDTO(inventory.getSku(), available);
    }
}
