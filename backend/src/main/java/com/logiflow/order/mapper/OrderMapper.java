package com.logiflow.order.mapper;

import com.logiflow.order.dto.OrderItemResponseDTO;
import com.logiflow.order.dto.OrderResponseDTO;
import com.logiflow.order.model.Order;
import com.logiflow.order.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDTO toDto(Order order) {
        if (order == null) return null;
        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerName(),
                order.getStatus(),
                order.getCreatedAt(),
                itemDTOs
        );
    }

    public OrderItemResponseDTO toItemDto(OrderItem item) {
        if (item == null) return null;
        return new OrderItemResponseDTO(
                item.getSku(),
                item.getQuantity(),
                item.getPriceAtTimeOfOrder()
        );
    }
}

