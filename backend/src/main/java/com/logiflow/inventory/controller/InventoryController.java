package com.logiflow.inventory.controller;

import com.logiflow.inventory.dto.InventoryResponseDTO;
import com.logiflow.inventory.dto.StockAdjustmentDTO;
import com.logiflow.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku}")
    public InventoryResponseDTO getInventory(@PathVariable String sku) {
        return inventoryService.getAvailableInventory(sku);
    }

    @PostMapping("/stock")
    public void adjustStock(@Valid @RequestBody StockAdjustmentDTO dto) {
        switch(dto.type()) {
            case ADD -> inventoryService.addStock(dto.sku(), dto.adjustmentQuantity());
            case REMOVE -> inventoryService.reserveStock(dto.sku(), dto.adjustmentQuantity());
        }
    }
}
