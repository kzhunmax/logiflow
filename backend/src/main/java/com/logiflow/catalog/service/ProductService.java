package com.logiflow.catalog.service;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.dto.ProductResponseDTO;
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

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, String search) {
        Page<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCaseAndActiveTrueOrSkuContainingIgnoreCaseAndActiveTrue(search, search, pageable);
            return products.map(this::mapToDTO);
        }
        products = productRepository.findByActiveTrue(pageable);
        return products.map(this::mapToDTO);
    }

    @CachePut(value = "products", key = "#result.id")
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = mapToEntity(dto);
        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @CachePut(value = "products", key = "#id")
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO dto) {
        Product product = findProductOrThrow(id);

        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setPrice(dto.price());
        product.setAttributes(dto.attributes());

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getProductById(String id) {
        return mapToDTO(findProductOrThrow(id));
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

    private ProductResponseDTO mapToDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getAttributes(),
                product.getActive()
        );
    }
}
