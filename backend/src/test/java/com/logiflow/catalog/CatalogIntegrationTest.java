package com.logiflow.catalog;

import com.logiflow.catalog.model.Product;
import com.logiflow.catalog.repository.ProductRepository;
import com.logiflow.config.TestcontainersConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("Catalog Integration Tests")
public class CatalogIntegrationTest extends TestcontainersConfiguration {

    private static final String PRODUCT_SKU = "LAPTOP-001";
    private static final String PRODUCT_NAME = "Gaming Laptop";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("1499.99");
    private static final String RAM_ATTRIBUTE_KEY = "ram";
    private static final String RAM_ATTRIBUTE_VALUE = "16GB";

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("Product Attributes Tests")
    class ProductAttributesTest {

        @Test
        @DisplayName("should save and retrieve product with attributes")
        void shouldSaveAndRetrieveProductWithAttributes() {
            // Given
            Map<String, Object> attributes = Map.of(
                    RAM_ATTRIBUTE_KEY, RAM_ATTRIBUTE_VALUE,
                    "storage", "512gb"
            );
            Product product = Product.builder()
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .price(PRODUCT_PRICE)
                    .active(true)
                    .attributes(attributes)
                    .build();

            // When
            Product savedProduct = productRepository.save(product);

            // Then
            assertThat(savedProduct.getId()).isNotNull();

            Optional<Product> fetchedProduct = productRepository.findById(savedProduct.getId());

            assertThat(fetchedProduct).isPresent();
            assertThat(fetchedProduct.get().getAttributes()).isNotNull();
            assertThat(fetchedProduct.get().getAttributes().get(RAM_ATTRIBUTE_KEY)).isEqualTo(RAM_ATTRIBUTE_VALUE);
        }

        @Test
        @DisplayName("should persist all product attributes correctly")
        void shouldPersistAllProductAttributesCorrectly() {
            // Given
            Map<String, Object> attributes = Map.of(RAM_ATTRIBUTE_KEY, RAM_ATTRIBUTE_VALUE);

            Product product = Product.builder()
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .price(PRODUCT_PRICE)
                    .active(true)
                    .attributes(attributes)
                    .build();

            // When
            productRepository.save(product);
            Product fetchedProduct = productRepository.findBySkuAndActiveTrue(PRODUCT_SKU).orElseThrow();

            // Then
            assertThat(fetchedProduct.getSku()).isEqualTo(PRODUCT_SKU);
            assertThat(fetchedProduct.getName()).isEqualTo(PRODUCT_NAME);
            assertThat(fetchedProduct.getPrice()).isEqualByComparingTo(PRODUCT_PRICE);
            assertThat(fetchedProduct.getActive()).isTrue();
            assertThat(fetchedProduct.getAttributes()).isEqualTo(Map.of(RAM_ATTRIBUTE_KEY, RAM_ATTRIBUTE_VALUE));
        }

        @Test
        @DisplayName("should update product attributes")
        void shouldUpdateProductAttributes() {
            // Given
            Product product = Product.builder()
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .price(PRODUCT_PRICE)
                    .active(true)
                    .attributes(Map.of(RAM_ATTRIBUTE_KEY, "8gb"))
                    .build();

            Product savedProduct = productRepository.save(product);

            // When
            savedProduct.setAttributes(Map.of(RAM_ATTRIBUTE_KEY, RAM_ATTRIBUTE_VALUE));
            productRepository.save(savedProduct);

            // Then
            Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
            assertThat(updatedProduct.getAttributes().get(RAM_ATTRIBUTE_KEY))
                    .isEqualTo(RAM_ATTRIBUTE_VALUE);
        }
    }
    @Nested
    @DisplayName("Product CRUD Operations")
    class ProductCrudOperationsTests {

        @Test
        @DisplayName("should find product by SKU")
        void shouldFindProductBySku() {
            // Given
            Product product = Product.builder()
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .price(PRODUCT_PRICE)
                    .active(true)
                    .attributes(Map.of(RAM_ATTRIBUTE_KEY, RAM_ATTRIBUTE_VALUE))
                    .build();

            productRepository.save(product);

            // When
            Optional<Product> foundProduct = productRepository.findBySkuAndActiveTrue(PRODUCT_SKU);

            // Then
            Assertions.assertThat(foundProduct).isPresent();
            assertThat(foundProduct.get().getAttributes().get(RAM_ATTRIBUTE_KEY))
                    .isEqualTo(RAM_ATTRIBUTE_VALUE);
        }

        @Test
        @DisplayName("should return empty when product not found by SKU")
        void shouldReturnEmptyWhenProductNotFoundBySku() {
            // When
            Optional<Product> foundProduct = productRepository.findBySkuAndActiveTrue("NON-EXISTENT-SKU");

            // Then
            Assertions.assertThat(foundProduct).isEmpty();
        }
    }
}
