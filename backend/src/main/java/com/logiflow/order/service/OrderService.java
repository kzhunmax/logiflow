package com.logiflow.order.service;

import com.logiflow.catalog.dto.ProductResponseDTO;
import com.logiflow.catalog.service.ProductService;
import com.logiflow.inventory.service.InventoryService;
import com.logiflow.order.dto.OrderItemRequestDTO;
import com.logiflow.order.dto.OrderItemResponseDTO;
import com.logiflow.order.dto.OrderRequestDTO;
import com.logiflow.order.dto.OrderResponseDTO;
import com.logiflow.order.model.Order;
import com.logiflow.order.model.OrderItem;
import com.logiflow.order.model.OrderStatus;
import com.logiflow.order.repository.OrderRepository;
import com.logiflow.shared.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        List<String> requestedSkus = request.items().stream()
                .map(OrderItemRequestDTO::sku)
                .toList();

        // 1. Validate SKUs exist and get prices
        Map<String, BigDecimal> priceMap = validateAndGetPrices(requestedSkus);

        // 2. Reserve stock for each item (within same transaction)
        for (OrderItemRequestDTO item : request.items()) {
            log.info("Reserving {} units of SKU: {}", item.quantity(), item.sku());
            inventoryService.reserveStock(item.sku(), item.quantity());
        }

        // 3. Build order items with price at time of order
        List<OrderItem> orderItems = request.items().stream()
                .map(item -> OrderItem.builder()
                        .sku(item.sku())
                        .quantity(item.quantity())
                        .priceAtTimeOfOrder(priceMap.get(item.sku()))
                        .build())
                .toList();

        // 4. Create and save order with PENDING status
        Order order = Order.builder()
                .customerName(request.customerName())
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>(orderItems))
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} for customer: {}", savedOrder.getId(), savedOrder.getCustomerName());

        return mapToResponseDTO(savedOrder);
    }

    private Map<String, BigDecimal> validateAndGetPrices(List<String> requestedSkus) {
        List<ProductResponseDTO> products = productService.findBySkus(requestedSkus);

        Set<String> foundSkus = products.stream()
                .map(ProductResponseDTO::sku)
                .collect(Collectors.toSet());

        List<String> missingSkus = requestedSkus.stream()
                .filter(sku -> !foundSkus.contains(sku))
                .toList();

        if (!missingSkus.isEmpty()) {
            throw ProductNotFoundException.forSkus(missingSkus);
        }

        return products.stream()
                .collect(Collectors.toMap(ProductResponseDTO::sku, ProductResponseDTO::price));
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getSku(),
                        item.getQuantity(),
                        item.getPriceAtTimeOfOrder()))
                .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerName(),
                order.getStatus(),
                order.getCreatedAt(),
                itemDTOs
        );
    }
}
