package com.logiflow.catalog.service;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.dto.ProductResponseDTO;
import com.logiflow.catalog.model.Product;
import com.logiflow.catalog.repository.ProductRepository;
import com.logiflow.shared.event.ProductCreatedEvent;
import com.logiflow.shared.event.ProductSkuUpdatedEvent;
import com.logiflow.shared.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, String search) {
        Page<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCaseAndActiveTrueOrSkuContainingIgnoreCaseAndActiveTrue(search, search, pageable);
            return products.map(this::mapToDTO);
        }
        products = productRepository.findByActiveTrue(pageable);
        return products.map(this::mapToDTO);
    }

    @Transactional
    @CachePut(value = "products", key = "#result.id")
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = mapToEntity(dto);
        Product savedProduct = productRepository.save(product);
        log.info("Publishing ProductCreatedEvent for SKU: {}", savedProduct.getSku());
        eventPublisher.publishEvent(new ProductCreatedEvent(savedProduct.getId(), savedProduct.getSku()));
        return mapToDTO(savedProduct);
    }

    @Transactional
    @CachePut(value = "products", key = "#id")
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO dto) {
        Product product = findProductOrThrow(id);

        String oldSku = product.getSku();
        boolean skuChanged = !oldSku.equals(dto.sku());

        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setPrice(dto.price());
        product.setAttributes(dto.attributes());

        Product savedProduct = productRepository.save(product);

        if (skuChanged) {
            log.info("Publishing ProductSkuUpdatedEvent: oldSku={}, newSku={}", oldSku, dto.sku());
            eventPublisher.publishEvent(new ProductSkuUpdatedEvent(savedProduct.getId(), oldSku, dto.sku()));
        }

        return mapToDTO(savedProduct);
    }

    @Transactional
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
