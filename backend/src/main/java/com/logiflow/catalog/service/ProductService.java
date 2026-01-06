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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== Query Methods ====================

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, String search) {
        Page<Product> products = hasSearch(search)
                ? searchProducts(search, pageable)
                : productRepository.findByActiveTrue(pageable);

        return products.map(this::toResponseDTO);
    }

    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(String id) {
        return toResponseDTO(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findBySkus(List<String> skus) {
        return productRepository.findBySkuInAndActiveTrue(skus).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==================== Command Methods ====================

    @Transactional
    @CachePut(value = "products", key = "#result.id")
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = toEntity(dto);
        Product savedProduct = productRepository.save(product);

        publishEvent(new ProductCreatedEvent(savedProduct.getId(), savedProduct.getSku()));

        return toResponseDTO(savedProduct);
    }

    @Transactional
    @CachePut(value = "products", key = "#id")
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO dto) {
        Product product = findByIdOrThrow(id);
        String oldSku = product.getSku();

        updateProductFields(product, dto);
        Product savedProduct = productRepository.save(product);

        publishSkuUpdateEventIfChanged(oldSku, dto.sku(), savedProduct.getId());

        return toResponseDTO(savedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        Product product = findByIdOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    // ==================== Lookup Helpers ====================

    private Product findByIdOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ProductNotFoundException.forId(id));
    }

    // ==================== Search Helpers ====================

    private boolean hasSearch(String search) {
        return search != null && !search.isBlank();
    }

    private Page<Product> searchProducts(String search, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrueOrSkuContainingIgnoreCaseAndActiveTrue(
                search, search, pageable);
    }

    // ==================== Update Helpers ====================

    private void updateProductFields(Product product, ProductRequestDTO dto) {
        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setPrice(dto.price());
        product.setAttributes(dto.attributes());
    }

    // ==================== Event Helpers ====================

    private void publishEvent(Object event) {
        log.info("Publishing event: {}", event.getClass().getSimpleName());
        eventPublisher.publishEvent(event);
    }

    private void publishSkuUpdateEventIfChanged(String oldSku, String newSku, String productId) {
        if (!oldSku.equals(newSku)) {
            log.info("SKU changed from {} to {}", oldSku, newSku);
            publishEvent(new ProductSkuUpdatedEvent(productId, oldSku, newSku));
        }
    }

    // ==================== Mapping Helpers ====================

    private Product toEntity(ProductRequestDTO dto) {
        return Product.builder()
                .name(dto.name())
                .sku(dto.sku())
                .price(dto.price())
                .attributes(dto.attributes())
                .active(true)
                .build();
    }

    private ProductResponseDTO toResponseDTO(Product product) {
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
