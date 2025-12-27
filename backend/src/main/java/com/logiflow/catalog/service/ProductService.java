package com.logiflow.catalog.service;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.model.Product;
import com.logiflow.catalog.repository.ProductRepository;
import com.logiflow.shared.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> getAllProducts(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseAndActiveTrue(search, search, pageable);
        }
        return productRepository.findByActiveTrue(pageable);
    }

    @CachePut(value = "products", key = "#result.id")
    public Product createProduct(ProductRequestDTO dto) {
        Product product = mapToEntity(dto);
        return productRepository.save(product);
    }

    @CachePut(value = "products", key = "#id")
    public Product updateProduct(String id, ProductRequestDTO dto) {
        Product product = findProductOrThrow(id);

        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setPrice(dto.price());
        product.setAttributes(dto.attributes());

        return productRepository.save(product);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(String id) {
        return findProductOrThrow(id);
    }

    private Product findProductOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
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
