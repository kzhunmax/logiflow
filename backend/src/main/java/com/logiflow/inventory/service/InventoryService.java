package com.logiflow.inventory.service;

import com.logiflow.inventory.dto.InventoryResponseDTO;
import com.logiflow.inventory.model.Inventory;
import com.logiflow.inventory.repository.InventoryRepository;
import com.logiflow.shared.exception.InsufficientStockException;
import com.logiflow.shared.exception.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;


    public List<InventoryResponseDTO> getInventoriesBySKUs(List<String> skus) {
        return inventoryRepository.findBySkuIn(skus).stream()
                .map(inventory -> new InventoryResponseDTO(
                        inventory.getSku(),
                        inventory.getQuantity() - inventory.getReserved()
                ))
                .toList();
    }

    @Transactional
    public void initializeInventory(String sku) {
        if (inventoryRepository.findBySku(sku).isPresent()) {
            log.info("Inventory already exists for SKU: {}", sku);
            return;
        }
        createInventory(sku, 0);
        log.info("Initialized inventory for SKU: {}", sku);
    }

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
            createInventory(sku, amount);
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

    @Transactional
    public void updateSku(String oldSku, String newSku) {
        Inventory inventory = inventoryRepository.findBySku(oldSku)
                .orElseThrow(() -> new InventoryNotFoundException(oldSku));
        inventory.setSku(newSku);
        inventoryRepository.save(inventory);
        log.info("Updated inventory SKU from {} to {}", oldSku, newSku);
    }

    private void createInventory(String sku, Integer quantity) {
        Inventory newInventory = Inventory.builder()
                .sku(sku)
                .quantity(quantity)
                .reserved(0)
                .build();
        inventoryRepository.save(newInventory);
    }
}
