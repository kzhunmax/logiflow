package com.logiflow.inventory.service;

import com.logiflow.config.TestcontainersConfiguration;
import com.logiflow.inventory.model.Inventory;
import com.logiflow.inventory.repository.InventoryRepository;
import com.logiflow.shared.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Inventory Concurrency Integration Tests ")
public class InventoryConcurrencyTest extends TestcontainersConfiguration {

    private static final String TEST_SKU = "CONCURRENT-SKU-001";

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
    }

    @Test
    @DisplayName("should handle concurrent reservations without negative stock")
    void shouldNeverAllowNegativeStock_UnderHighConcurrency() throws InterruptedException {
        // Given - Setup inventory with 10 items
        Inventory inventory = Inventory.builder()
                .sku(TEST_SKU)
                .quantity(10)
                .reserved(0)
                .build();
        inventoryRepository.save(inventory);

        int numberOfThreads = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads)) {
            // When - Each thread tries to reserve 1 item
            for (int i = 0; i < numberOfThreads; i++) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        inventoryService.reserveStock(TEST_SKU, 1);
                        successCount.incrementAndGet();
                    } catch (OptimisticLockingFailureException | InsufficientStockException e) {
                        // Expected for threads that lose the race
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completionLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            completionLatch.await();
        }
        // Then
        Inventory finalInventory = inventoryRepository.findBySku(TEST_SKU).orElseThrow();

        assertThat(finalInventory.getReserved())
                .as("Reserved should exactly match total quantity (10)")
                .isEqualTo(10);

        assertThat(successCount.get())
                .as("Should have exactly 10 successful reservations")
                .isEqualTo(10);
    }
}
