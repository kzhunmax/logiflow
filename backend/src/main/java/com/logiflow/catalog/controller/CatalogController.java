package com.logiflow.catalog.controller;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.model.Product;
import com.logiflow.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;

    @GetMapping
    public List<Product> findAll() {
        return productService.findAll();
    }

    @PostMapping
    public ResponseEntity<@NonNull Product> create(@Valid @RequestBody ProductRequestDTO dto) {
        Product created = productService.create(dto);
        return ResponseEntity.ok(created);
    }
}
