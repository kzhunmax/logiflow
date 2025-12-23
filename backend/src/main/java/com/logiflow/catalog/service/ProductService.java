package com.logiflow.catalog.service;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.model.Product;
import com.logiflow.catalog.repository.ProductRepository;
import com.logiflow.shared.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    public Product createProduct(ProductRequestDTO dto) {
        Product product = mapToEntity(dto);
        return productRepository.save(product);
    }

    public Product updateProduct(String id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setPrice(dto.price());

        product.setAttributes(dto.attributes());

        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(false);
        productRepository.save(product);
    }

    private Product mapToEntity(ProductRequestDTO dto) {
        return Product.builder()
                .name(dto.name())
                .sku(dto.sku())
                .price(dto.price())
                .attributes(dto.attributes())
                .active(true)
                .build();
    }
}
