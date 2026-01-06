package com.logiflow.inventory.mapper;

import com.logiflow.inventory.dto.InventoryResponseDTO;
import com.logiflow.inventory.model.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponseDTO toDto(Inventory inventory) {
        if (inventory == null) return null;
        int available = inventory.getQuantity() - inventory.getReserved();
        return new InventoryResponseDTO(inventory.getSku(), available);
    }
}

