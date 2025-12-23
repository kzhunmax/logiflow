package com.logiflow.inventory.service;

import com.logiflow.inventory.model.Inventory;
import com.logiflow.inventory.repository.InventoryRepository;
import com.logiflow.shared.exception.InsufficientStockException;
import com.logiflow.shared.exception.InventoryNotFoundException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    private static final String TEST_SKU = "SKU-001";
    private static final Long TEST_ID = 1L;
    private static final Integer INITIAL_QUANTITY = 100;
    private static final Integer INITIAL_RESERVED = 10;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Captor
    private ArgumentCaptor<Inventory> inventoryCaptor;

    private Inventory existingInventory;

    @BeforeEach
    void setUp() {
        existingInventory = Inventory.builder()
                .id(TEST_ID)
                .sku(TEST_SKU)
                .quantity(INITIAL_QUANTITY)
                .reserved(INITIAL_RESERVED)
                .build();
    }

    @Nested
    @DisplayName("addStock")
    class AddStock {

        @Test
        @DisplayName("should add stock to existing inventory when SKU exists")
        void shouldAddStockToExistingInventory_WhenSkuExists() {
            // Given
            Integer amountToAdd = 50;
            given(inventoryRepository.findBySku(TEST_SKU)).willReturn(Optional.of(existingInventory));

            // When
            inventoryService.addStock(TEST_SKU, amountToAdd);

            // Then
            then(inventoryRepository).should().save(inventoryCaptor.capture());
            Inventory savedInventory = inventoryCaptor.getValue();

            assertThat(savedInventory.getSku()).isEqualTo(TEST_SKU);
            assertThat(savedInventory.getQuantity()).isEqualTo(INITIAL_QUANTITY + amountToAdd);
        }

        @Test
        @DisplayName("should create new inventory when SKU does not exist")
        void shouldCreateNewInventory_WhenSkuDoesNotExist() {
            // Given
            String newSku = "NEW-SKU-001";
            Integer amount = 25;
            given(inventoryRepository.findBySku(newSku)).willReturn(Optional.empty());

            // When
            inventoryService.addStock(newSku, amount);

            // Then
            then(inventoryRepository).should().save(inventoryCaptor.capture());
            Inventory savedInventory = inventoryCaptor.getValue();

            assertThat(savedInventory.getSku()).isEqualTo(newSku);
            assertThat(savedInventory.getQuantity()).isEqualTo(amount);
        }

        @Test
        @DisplayName("should handle adding zero stock to existing inventory")
        void shouldHandleZeroStock_WhenAddingToExistingInventory() {
            // Given
            Integer amountToAdd = 0;
            given(inventoryRepository.findBySku(TEST_SKU)).willReturn(Optional.of(existingInventory));

            // When
            inventoryService.addStock(TEST_SKU, amountToAdd);

            // Then
            then(inventoryRepository).should().save(inventoryCaptor.capture());
            Inventory savedInventory = inventoryCaptor.getValue();

            assertThat(savedInventory.getQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("should preserve existing inventory properties when adding stock")
        void shouldPreserveExistingProperties_WhenAddingStock() {
            // Given
            Integer amountToAdd = 30;
            given(inventoryRepository.findBySku(TEST_SKU)).willReturn(Optional.of(existingInventory));

            // When
            inventoryService.addStock(TEST_SKU, amountToAdd);

            // Then
            then(inventoryRepository).should().save(inventoryCaptor.capture());
            Inventory savedInventory = inventoryCaptor.getValue();

            assertThat(savedInventory.getId()).isEqualTo(existingInventory.getId());
            assertThat(savedInventory.getReserved()).isEqualTo(INITIAL_RESERVED);
        }
    }

    @Nested
    @DisplayName("reserveStock")
    class ReserveStock {

        @Test
        @DisplayName("should reserve stock when sufficient quantity is available")
        void shouldReserveStock_WhenSufficientQuantityAvailable() {
            // Given
            Integer amountToReserve = 50;
            given(inventoryRepository.findBySkuForUpdate(TEST_SKU)).willReturn(Optional.of(existingInventory));

            // When
            inventoryService.reserveStock(TEST_SKU, amountToReserve);

            // Then
            then(inventoryRepository).should().save(inventoryCaptor.capture());
            Inventory savedInventory = inventoryCaptor.getValue();

            assertThat(savedInventory.getReserved()).isEqualTo(INITIAL_RESERVED + amountToReserve);
        }

        @Test
        @DisplayName("should reserve stock when exactly matching available quantity")
        void shouldReserveStock_WhenExactlyMatchingAvailableQuantity() {
            // Given
            Integer availableStock = INITIAL_QUANTITY - INITIAL_RESERVED; // 90
            given(inventoryRepository.findBySkuForUpdate(TEST_SKU)).willReturn(Optional.of(existingInventory));

            // When
            inventoryService.reserveStock(TEST_SKU, availableStock);

            // Then
            then(inventoryRepository).should().save(inventoryCaptor.capture());
            Inventory savedInventory = inventoryCaptor.getValue();

            assertThat(savedInventory.getReserved()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("should throw InsufficientStockException when requesting more than available")
        void shouldThrowException_WhenInsufficientStockAvailable() {
            // Given
            Integer excessiveAmount = INITIAL_QUANTITY - INITIAL_RESERVED + 1; // 91
            given(inventoryRepository.findBySkuForUpdate(TEST_SKU)).willReturn(Optional.of(existingInventory));

            // When / Then
            assertThatThrownBy(() -> inventoryService.reserveStock(TEST_SKU, excessiveAmount))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessage("Insufficient stock available to reserve");

            then(inventoryRepository).should(never()).save(any(Inventory.class));
        }

        @Test
        @DisplayName("should not save inventory when SKU does not exist")
        void shouldNotSave_WhenSkuDoesNotExist() {
            // Given
            String nonExistentSku = "NON-EXISTENT-SKU";
            given(inventoryRepository.findBySkuForUpdate(nonExistentSku)).willReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> inventoryService.reserveStock(nonExistentSku, 10))
                    .isInstanceOf(InventoryNotFoundException.class);

            // Then
            then(inventoryRepository).should(never()).save(any(Inventory.class));
        }

        @Test
        @DisplayName("should handle zero reservation amount")
        void shouldHandleZeroReservation() {
            // Given
            Integer amountToReserve = 0;
            given(inventoryRepository.findBySkuForUpdate(TEST_SKU)).willReturn(Optional.of(existingInventory));

            // When
            inventoryService.reserveStock(TEST_SKU, amountToReserve);

            // Then
            then(inventoryRepository).should().save(inventoryCaptor.capture());
            Inventory savedInventory = inventoryCaptor.getValue();

            assertThat(savedInventory.getReserved()).isEqualTo(INITIAL_RESERVED);
        }

        @Test
        @DisplayName("should preserve inventory properties when reserving stock")
        void shouldPreserveInventoryProperties_WhenReservingStock() {
            // Given
            Integer amountToReserve = 20;
            given(inventoryRepository.findBySkuForUpdate(TEST_SKU)).willReturn(Optional.of(existingInventory));

            // When
            inventoryService.reserveStock(TEST_SKU, amountToReserve);

            // Then
            then(inventoryRepository).should().save(inventoryCaptor.capture());
            Inventory savedInventory = inventoryCaptor.getValue();

            assertThat(savedInventory.getId()).isEqualTo(existingInventory.getId());
            assertThat(savedInventory.getSku()).isEqualTo(TEST_SKU);
            assertThat(savedInventory.getQuantity()).isEqualTo(INITIAL_QUANTITY);
        }

        @Test
        @DisplayName("should throw exception when all stock is already reserved")
        void shouldThrowException_WhenAllStockAlreadyReserved() {
            // Given
            Inventory fullyReservedInventory = Inventory.builder()
                    .id(2L)
                    .sku(TEST_SKU)
                    .quantity(100)
                    .reserved(100)
                    .build();
            given(inventoryRepository.findBySkuForUpdate(TEST_SKU)).willReturn(Optional.of(fullyReservedInventory));

            // When / Then
            assertThatThrownBy(() -> inventoryService.reserveStock(TEST_SKU, 1))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessage("Insufficient stock available to reserve");

            then(inventoryRepository).should(never()).save(any(Inventory.class));
        }
    }
}
