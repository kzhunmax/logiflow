package com.logiflow.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequestDTO(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        List<OrderItemRequestDTO> items
) {
}
