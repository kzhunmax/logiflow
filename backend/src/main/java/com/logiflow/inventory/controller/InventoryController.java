package com.logiflow.inventory.controller;

import com.logiflow.inventory.dto.InventoryResponseDTO;
import com.logiflow.inventory.dto.StockAdjustmentDTO;
import com.logiflow.inventory.service.InventoryService;
import com.logiflow.shared.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Operations for managing product inventory and stock levels")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku}")
    @Operation(summary = "Get inventory by SKU", description = "Retrieves the current available inventory for a product by its SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory found",
                    content = @Content(schema = @Schema(implementation = InventoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found for the specified SKU",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryResponseDTO> getInventory(
            @Parameter(description = "Stock Keeping Unit (SKU) identifier") @PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.getAvailableInventory(sku));
    }

    @PostMapping("/stock")
    @Operation(summary = "Adjust stock levels", description = "Adds or removes stock for a product. Use ADD to increase stock or REMOVE to reserve/decrease stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Stock adjusted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data - validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found for the specified SKU",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient stock for removal operation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> adjustStock(
            @Valid @RequestBody StockAdjustmentDTO dto) {
        switch(dto.type()) {
            case ADD -> inventoryService.addStock(dto.sku(), dto.adjustmentQuantity());
            case REMOVE -> inventoryService.reserveStock(dto.sku(), dto.adjustmentQuantity());
        }
        return ResponseEntity.noContent().build();
    }
}
