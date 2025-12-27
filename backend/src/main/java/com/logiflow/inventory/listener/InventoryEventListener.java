package com.logiflow.inventory.listener;

import com.logiflow.inventory.service.InventoryService;
import com.logiflow.shared.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {


    private final InventoryService inventoryService;

    @EventListener
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent for productId: {}, SKU: {}", event.productId(), event.sku());
        inventoryService.initializeInventory(event.sku());
    }
}
