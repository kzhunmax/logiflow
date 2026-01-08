package com.logiflow.inventory.service;

import com.logiflow.inventory.dto.InventoryResponseDTO;
import com.logiflow.inventory.mapper.InventoryMapper;
import com.logiflow.inventory.model.Inventory;
import com.logiflow.inventory.repository.InventoryRepository;
import com.logiflow.shared.exception.InsufficientStockException;
import com.logiflow.shared.exception.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> getInventoriesBySKUs(List<String> skus) {
        return inventoryRepository.findBySkuIn(skus).stream()
                .map(inventoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "inventory", key = "#sku")
    public InventoryResponseDTO getAvailableInventory(String sku) {
        return inventoryMapper.toDto(findBySkuOrThrow(sku));
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

    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            backoff = @Backoff(delay = 50) // 50 ms delay between retries
    )
    @Transactional
    @CacheEvict(value = "inventory", key = "#sku")
    public void addStock(String sku, Integer amount) {
        addOrCreateStock(sku, amount);
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "#sku")
    public void reserveStock(String sku, Integer amount) {
        Inventory inventory = findBySkuForUpdateOrThrow(sku);
        validateSufficientStock(inventory, amount);

        inventory.setReserved(inventory.getReserved() + amount);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void updateSku(String oldSku, String newSku) {
        Inventory inventory = findBySkuOrThrow(oldSku);
        inventory.setSku(newSku);
        inventoryRepository.save(inventory);
        log.info("Updated inventory SKU from {} to {}", oldSku, newSku);
    }

    private Inventory findBySkuOrThrow(String sku) {
        return inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));
    }

    private Inventory findBySkuForUpdateOrThrow(String sku) {
        return inventoryRepository.findBySkuForUpdate(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));
    }

    private void addOrCreateStock(String sku, Integer amount) {
        inventoryRepository.findBySku(sku)
                .ifPresentOrElse(
                        inventory -> incrementQuantity(inventory, amount),
                        () -> createInventory(sku, amount)
                );
    }

    private void incrementQuantity(Inventory inventory, Integer amount) {
        inventory.setQuantity(inventory.getQuantity() + amount);
        inventoryRepository.save(inventory);
    }

    private void createInventory(String sku, Integer quantity) {
        Inventory newInventory = Inventory.builder()
                .sku(sku)
                .quantity(quantity)
                .reserved(0)
                .build();
        inventoryRepository.save(newInventory);
    }

    private void validateSufficientStock(Inventory inventory, Integer amount) {
        int available = inventory.getQuantity() - inventory.getReserved();
        if (available < amount) {
            throw new InsufficientStockException("Insufficient stock available to reserve");
        }
    }
}
