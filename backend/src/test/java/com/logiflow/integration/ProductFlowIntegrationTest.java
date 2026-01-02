package com.logiflow.integration;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.repository.ProductRepository;
import com.logiflow.catalog.service.ProductService;
import com.logiflow.config.TestcontainersConfiguration;
import com.logiflow.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("Product-Inventory Workflow Integration Tests")
public class ProductFlowIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("should create inventory entry when product is created")
    void shouldCreateInventoryEntry_WhenProductIsCreated() {
        // Given
        String sku = "SKU-TEST-001";
        ProductRequestDTO request = new ProductRequestDTO(
                "Test Product",
                sku,
                BigDecimal.valueOf(49.99),
                Map.of("color", "red", "size", "L")
        );

        // When
        productService.createProduct(request);

        // Then
        assertThat(productRepository.count()).isEqualTo(1);
        assertThat(productRepository.findBySkuAndActiveTrue(sku)).isPresent();
        assertThat(inventoryRepository.count()).isEqualTo(1);
        assertThat(inventoryRepository.findBySku(sku))
                .isPresent()
                .hasValueSatisfying(inventory -> {
                    assertThat(inventory.getSku()).isEqualTo(sku);
                    assertThat(inventory.getQuantity()).isZero();
                    assertThat(inventory.getReserved()).isZero();
        });
    }
}
