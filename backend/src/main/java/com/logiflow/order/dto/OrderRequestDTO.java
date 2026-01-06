package com.logiflow.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request payload for creating a new order")
public record OrderRequestDTO(
        @Schema(description = "Name of the customer placing the order", example = "John Doe")
        @NotBlank(message = "Customer name is required")
        String customerName,

        @Schema(description = "List of items to order")
        @NotEmpty(message = "Order must have at least one item")
        @Valid
        List<OrderItemRequestDTO> items
) {
}
