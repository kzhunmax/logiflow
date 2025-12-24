package com.logiflow.catalog.service;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.model.Product;
import com.logiflow.catalog.repository.ProductRepository;
import com.logiflow.shared.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    private static final String PRODUCT_ID = "prod-123";
    private static final String NON_EXISTING_PRODUCT_ID = "prod-999";
    private static final String PRODUCT_NAME = "Sample Product";
    private static final String PRODUCT_SKU = "SKU-001";
    private static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(19.99);
    private static final Map<String, Object> PRODUCT_ATTRIBUTES = Map.of("color", "red", "size", "M");

    private static final String UPDATED_NAME = "Updated Product";
    private static final String UPDATED_SKU = "SKU-002";
    private static final BigDecimal UPDATED_PRICE = BigDecimal.valueOf(29.99);
    private static final Map<String, Object> UPDATED_ATTRIBUTES = Map.of("color", "blue");

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private Product activeProduct;

    @BeforeEach
    void setUp() {
        activeProduct = Product.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .sku(PRODUCT_SKU)
                .price(PRODUCT_PRICE)
                .attributes(PRODUCT_ATTRIBUTES)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("should return active products when no search term")
        void shouldReturnAllActiveProducts_WhenNoSearchTerm() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> expectedPage = new PageImpl<>(List.of(activeProduct), pageable, 1);
            given(productRepository.findByActiveTrue(pageable)).willReturn(expectedPage);

            // When
            Page<Product> result = productService.getAllProducts(pageable, null);

            // Then
            then(productRepository).should().findByActiveTrue(pageable);
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            Product returnedProduct = result.getContent().getFirst();
            assertThat(returnedProduct.getId()).isEqualTo(PRODUCT_ID);
            assertThat(returnedProduct.getName()).isEqualTo(PRODUCT_NAME);
            assertThat(returnedProduct.getActive()).isTrue();
        }

        @Test
        @DisplayName("should return active products when search term is provided")
        void shouldReturnAllActiveProducts_WhenSearchTermProvided() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            String searchTerm = "Sample";
            Page<Product> expectedPage = new PageImpl<>(List.of(activeProduct), pageable, 1);
            given(productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseAndActiveTrue(searchTerm, searchTerm, pageable)).willReturn(expectedPage);

            // When
            Page<Product> result = productService.getAllProducts(pageable, searchTerm);

            // Then
            then(productRepository).should().findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseAndActiveTrue(searchTerm, searchTerm, pageable);
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            Product returnedProduct = result.getContent().getFirst();
            assertThat(returnedProduct.getId()).isEqualTo(PRODUCT_ID);
            assertThat(returnedProduct.getName()).isEqualTo(PRODUCT_NAME);
            assertThat(returnedProduct.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {
        @Test
        @DisplayName("should return product when found by ID")
        void shouldReturnProduct_WhenFoundById() {
            // Given
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(activeProduct));

            // When
            Product result = productService.getProductById(PRODUCT_ID);

            // Then
            then(productRepository).should().findById(PRODUCT_ID);
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);
            assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when product does not exist")
        void shouldThrowException_WhenProductDoesNotExist() {
            // Given
            given(productRepository.findById(NON_EXISTING_PRODUCT_ID)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> productService.getProductById(NON_EXISTING_PRODUCT_ID))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(NON_EXISTING_PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("should create and return new product")
        void shouldCreateAndReturnNewProduct() {
            // Given
            ProductRequestDTO requestDTO = new ProductRequestDTO(
                    PRODUCT_NAME, PRODUCT_SKU, PRODUCT_PRICE, PRODUCT_ATTRIBUTES
            );
            given(productRepository.save(any(Product.class))).willReturn(activeProduct);

            // When
            Product result = productService.createProduct(requestDTO);

            // Then
            then(productRepository).should().save(productCaptor.capture());

            Product capturedProduct = productCaptor.getValue();
            assertThat(capturedProduct.getName()).isEqualTo(PRODUCT_NAME);
            assertThat(capturedProduct.getSku()).isEqualTo(PRODUCT_SKU);
            assertThat(capturedProduct.getPrice()).isEqualTo(PRODUCT_PRICE);
            assertThat(capturedProduct.getAttributes()).isEqualTo(PRODUCT_ATTRIBUTES);
            assertThat(capturedProduct.getActive()).isTrue();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("should update all product fields")
        void shouldUpdateAllProductFields() {
            // Given
            ProductRequestDTO updateDTO = new ProductRequestDTO(
                    UPDATED_NAME, UPDATED_SKU, UPDATED_PRICE, UPDATED_ATTRIBUTES
            );
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(activeProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            Product result = productService.updateProduct(PRODUCT_ID, updateDTO);

            // Then
            then(productRepository).should().findById(PRODUCT_ID);
            then(productRepository).should().save(productCaptor.capture());

            Product capturedProduct = productCaptor.getValue();
            assertThat(capturedProduct.getName()).isEqualTo(UPDATED_NAME);
            assertThat(capturedProduct.getSku()).isEqualTo(UPDATED_SKU);
            assertThat(capturedProduct.getPrice()).isEqualTo(UPDATED_PRICE);
            assertThat(capturedProduct.getAttributes()).isEqualTo(UPDATED_ATTRIBUTES);

            assertThat(result.getName()).isEqualTo(UPDATED_NAME);
            assertThat(result.getPrice()).isEqualTo(UPDATED_PRICE);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when updating non-existing product")
        void shouldThrowException_WhenUpdatingNonExistingProduct() {
            // Given
            ProductRequestDTO updateDTO = new ProductRequestDTO(
                    UPDATED_NAME, UPDATED_SKU, UPDATED_PRICE, UPDATED_ATTRIBUTES
            );
            given(productRepository.findById(NON_EXISTING_PRODUCT_ID)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> productService.updateProduct(NON_EXISTING_PRODUCT_ID, updateDTO))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(NON_EXISTING_PRODUCT_ID);

            then(productRepository).should().findById(NON_EXISTING_PRODUCT_ID);
            then(productRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("should mark product as inactive when deleted")
        void shouldMarkProductAsInactive_WhenDeleted() {
            // Given
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(activeProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            productService.deleteProduct(PRODUCT_ID);

            // Then
            then(productRepository).should().findById(PRODUCT_ID);
            then(productRepository).should().save(productCaptor.capture());

            Product capturedProduct = productCaptor.getValue();
            assertThat(capturedProduct.getId()).isEqualTo(PRODUCT_ID);
            assertThat(capturedProduct.getActive()).isFalse();
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when deleting non-existing product")
        void shouldThrowException_WhenDeletingNonExistingProduct() {
            // Given
            given(productRepository.findById(NON_EXISTING_PRODUCT_ID)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> productService.deleteProduct(NON_EXISTING_PRODUCT_ID))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(NON_EXISTING_PRODUCT_ID);

            then(productRepository).should().findById(NON_EXISTING_PRODUCT_ID);
            then(productRepository).shouldHaveNoMoreInteractions();
        }
    }
}
