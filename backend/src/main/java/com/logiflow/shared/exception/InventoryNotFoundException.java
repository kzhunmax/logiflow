package com.logiflow.shared.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String sku) {
        super("Inventory not found for SKU: " + sku);
    }
}
