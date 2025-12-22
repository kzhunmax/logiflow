package com.logiflow.catalog.service;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.model.Product;
import com.logiflow.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product create(ProductRequestDTO dto) {
        Product product = mapToEntity(dto);
        return productRepository.save(product);
    }

    private Product mapToEntity(ProductRequestDTO dto) {
        return Product.builder()
                .name(dto.name())
                .sku(dto.sku())
                .price(dto.price())
                .attributes(dto.attributes())
                .build();
    }
}
