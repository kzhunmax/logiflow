package com.logiflow.catalog.controller;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.model.Product;
import com.logiflow.catalog.service.ProductService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/catalog/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Operations for managing catalog products")
public class CatalogController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Returns a paginated list of active products, optionally filtered by search term. Search matches against product name or SKU.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public Page<Product> getAllProducts(
            @Parameter(description = "Pageable parameters (page, size, sort)") Pageable pageable,
            @Parameter(description = "Optional search term for name or SKU") @RequestParam(required = false) String search) {
        return productService.getAllProducts(pageable, search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a single product by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID") @PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product in the catalog with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data - validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequestDTO dto) {
        Product created = productService.createProduct(dto);
        return ResponseEntity.created(URI.create("/api/catalog/products" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product", description = "Updates an existing product with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data - validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "Product ID") @PathVariable String id,
            @Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a product", description = "Soft-deletes a product by setting active=false, preserving data for audit purposes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
