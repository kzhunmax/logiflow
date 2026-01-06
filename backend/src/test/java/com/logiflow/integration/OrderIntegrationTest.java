package com.logiflow.integration;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.repository.ProductRepository;
import com.logiflow.catalog.service.ProductService;
import com.logiflow.config.TestcontainersConfiguration;
import com.logiflow.inventory.model.Inventory;
import com.logiflow.inventory.repository.InventoryRepository;
import com.logiflow.inventory.service.InventoryService;
import com.logiflow.order.dto.OrderItemRequestDTO;
import com.logiflow.order.dto.OrderRequestDTO;
import com.logiflow.order.dto.OrderResponseDTO;
import com.logiflow.order.model.OrderStatus;
import com.logiflow.order.repository.OrderRepository;
import com.logiflow.order.service.OrderService;
import com.logiflow.shared.exception.InsufficientStockException;
import com.logiflow.shared.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Order Integration Tests")
public class OrderIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("Full Order Flow Tests")
    class FullOrderFlowTests {

        @Test
        @DisplayName("should complete full order flow: create product -> add stock -> place order -> verify reserved")
        void shouldCompleteFullOrderFlow() {
            // Given - Create Product
            String sku = "SKU-ORDER-001";
            ProductRequestDTO productRequest = new ProductRequestDTO(
                    "Test Product",
                    sku,
                    BigDecimal.valueOf(29.99),
                    Map.of("color", "blue")
            );
            productService.createProduct(productRequest);

            // And - Add Stock
            int initialStock = 100;
            inventoryService.addStock(sku, initialStock);

            // Verify initial state
            Inventory inventoryBefore = inventoryRepository.findBySku(sku).orElseThrow();
            assertThat(inventoryBefore.getQuantity()).isEqualTo(initialStock);
            assertThat(inventoryBefore.getReserved()).isZero();

            // When - Place Order
            int orderQuantity = 5;
            OrderRequestDTO orderRequest = new OrderRequestDTO(
                    "John Doe",
                    List.of(new OrderItemRequestDTO(sku, orderQuantity))
            );
            OrderResponseDTO orderResponse = orderService.createOrder(orderRequest);

            // Then - Verify Order
            assertThat(orderResponse).isNotNull();
            assertThat(orderResponse.id()).isNotNull();
            assertThat(orderResponse.customerName()).isEqualTo("John Doe");
            assertThat(orderResponse.status()).isEqualTo(OrderStatus.PENDING);
            assertThat(orderResponse.items()).hasSize(1);
            assertThat(orderResponse.items().getFirst().sku()).isEqualTo(sku);
            assertThat(orderResponse.items().getFirst().quantity()).isEqualTo(orderQuantity);
            assertThat(orderResponse.items().getFirst().priceAtTimeOfOrder()).isEqualByComparingTo(BigDecimal.valueOf(29.99));

            // And - Verify Inventory Reserved
            Inventory inventoryAfter = inventoryRepository.findBySku(sku).orElseThrow();
            assertThat(inventoryAfter.getQuantity()).isEqualTo(initialStock);
            assertThat(inventoryAfter.getReserved()).isEqualTo(orderQuantity);

            // Verify available stock decreased
            int availableStock = inventoryAfter.getQuantity() - inventoryAfter.getReserved();
            assertThat(availableStock).isEqualTo(initialStock - orderQuantity);
        }

        @Test
        @DisplayName("should reserve stock for multiple items in single order")
        void shouldReserveStockForMultipleItems() {
            // Given - Create two products
            String sku1 = "SKU-MULTI-001";
            String sku2 = "SKU-MULTI-002";

            productService.createProduct(new ProductRequestDTO("Product 1", sku1, BigDecimal.valueOf(10.00), Map.of()));
            productService.createProduct(new ProductRequestDTO("Product 2", sku2, BigDecimal.valueOf(20.00), Map.of()));

            inventoryService.addStock(sku1, 50);
            inventoryService.addStock(sku2, 30);

            // When - Place order with multiple items
            OrderRequestDTO orderRequest = new OrderRequestDTO(
                    "Jane Doe",
                    List.of(
                            new OrderItemRequestDTO(sku1, 10),
                            new OrderItemRequestDTO(sku2, 5)
                    )
            );
            OrderResponseDTO orderResponse = orderService.createOrder(orderRequest);

            // Then - Verify both items reserved
            assertThat(orderResponse.items()).hasSize(2);

            Inventory inventory1 = inventoryRepository.findBySku(sku1).orElseThrow();
            Inventory inventory2 = inventoryRepository.findBySku(sku2).orElseThrow();

            assertThat(inventory1.getReserved()).isEqualTo(10);
            assertThat(inventory2.getReserved()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Order Failure Scenarios")
    class OrderFailureScenarios {

        @Test
        @DisplayName("should throw exception when SKU does not exist")
        void shouldThrowException_WhenSkuDoesNotExist() {
            // Given - No product created
            OrderRequestDTO orderRequest = new OrderRequestDTO(
                    "John Doe",
                    List.of(new OrderItemRequestDTO("NON-EXISTENT-SKU", 1))
            );

            // When/Then
            assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("NON-EXISTENT-SKU");

            // Verify no order was created
            assertThat(orderRepository.count()).isZero();
        }

        @Test
        @DisplayName("should throw exception when insufficient stock")
        void shouldThrowException_WhenInsufficientStock() {
            // Given - Create product with limited stock
            String sku = "SKU-LIMITED-001";
            productService.createProduct(new ProductRequestDTO("Limited Product", sku, BigDecimal.valueOf(99.99), Map.of()));
            inventoryService.addStock(sku, 5);

            // When - Try to order more than available
            OrderRequestDTO orderRequest = new OrderRequestDTO(
                    "John Doe",
                    List.of(new OrderItemRequestDTO(sku, 10))
            );

            // Then
            assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                    .isInstanceOf(InsufficientStockException.class);

            // Verify no reservation was made
            Inventory inventory = inventoryRepository.findBySku(sku).orElseThrow();
            assertThat(inventory.getReserved()).isZero();

            // Verify no order was created
            assertThat(orderRepository.count()).isZero();
        }

        @Test
        @DisplayName("should rollback all reservations when one item fails")
        void shouldRollbackReservations_WhenOneItemFails() {
            // Given - Two products, one with insufficient stock
            String sku1 = "SKU-ROLLBACK-001";
            String sku2 = "SKU-ROLLBACK-002";

            productService.createProduct(new ProductRequestDTO("Product 1", sku1, BigDecimal.valueOf(10.00), Map.of()));
            productService.createProduct(new ProductRequestDTO("Product 2", sku2, BigDecimal.valueOf(20.00), Map.of()));

            inventoryService.addStock(sku1, 100);
            inventoryService.addStock(sku2, 2); // Limited stock

            // When - Order where second item will fail
            OrderRequestDTO orderRequest = new OrderRequestDTO(
                    "John Doe",
                    List.of(
                            new OrderItemRequestDTO(sku1, 10),
                            new OrderItemRequestDTO(sku2, 5) // More than available
                    )
            );

            // Then
            assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                    .isInstanceOf(InsufficientStockException.class);

            // Verify first item reservation was rolled back
            Inventory inventory1 = inventoryRepository.findBySku(sku1).orElseThrow();
            Inventory inventory2 = inventoryRepository.findBySku(sku2).orElseThrow();

            assertThat(inventory1.getReserved()).isZero();
            assertThat(inventory2.getReserved()).isZero();

            // Verify no order was created
            assertThat(orderRepository.count()).isZero();
        }
    }
}

