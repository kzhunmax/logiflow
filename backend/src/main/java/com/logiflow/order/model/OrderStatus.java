package com.logiflow.order.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order lifecycle status")
public enum OrderStatus {
    @Schema(description = "Order created, awaiting confirmation")
    PENDING,

    @Schema(description = "Order confirmed and being processed")
    CONFIRMED,

    @Schema(description = "Order has been shipped")
    SHIPPED,

    @Schema(description = "Order was cancelled")
    CANCELLED
}
