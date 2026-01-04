package com.logiflow.shared.event;

public record ProductSkuUpdatedEvent(String productId, String oldSku, String newSku) {
}

