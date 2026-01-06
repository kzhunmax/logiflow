package com.logiflow.order.dto;

import com.logiflow.order.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String customerName,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponseDTO> items
) {
}
