package com.logiflow.order.service;

import com.logiflow.catalog.dto.ProductResponseDTO;
import com.logiflow.catalog.service.ProductService;
import com.logiflow.inventory.service.InventoryService;
import com.logiflow.order.dto.OrderItemRequestDTO;
import com.logiflow.order.dto.OrderRequestDTO;
import com.logiflow.order.dto.OrderResponseDTO;
import com.logiflow.order.mapper.OrderMapper;
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
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        List<String> skus = extractSkus(request.items());
        Map<String, BigDecimal> priceMap = validateAndGetPrices(skus);

        reserveStockForItems(request.items());

        Order order = buildOrder(request, priceMap);
        Order savedOrder = orderRepository.save(order);

        log.info("Order created with ID: {} for customer: {}", savedOrder.getId(), savedOrder.getCustomerName());
        return orderMapper.toDto(savedOrder);
    }

    private List<String> extractSkus(List<OrderItemRequestDTO> items) {
        return items.stream()
                .map(OrderItemRequestDTO::sku)
                .toList();
    }

    private Map<String, BigDecimal> validateAndGetPrices(List<String> requestedSkus) {
        List<ProductResponseDTO> products = productService.findBySkus(requestedSkus);

        validateAllSkusExist(requestedSkus, products);

        return products.stream()
                .collect(Collectors.toMap(ProductResponseDTO::sku, ProductResponseDTO::price));
    }

    private void validateAllSkusExist(List<String> requestedSkus, List<ProductResponseDTO> products) {
        Set<String> foundSkus = products.stream()
                .map(ProductResponseDTO::sku)
                .collect(Collectors.toSet());

        List<String> missingSkus = requestedSkus.stream()
                .filter(sku -> !foundSkus.contains(sku))
                .toList();

        if (!missingSkus.isEmpty()) {
            throw ProductNotFoundException.forSkus(missingSkus);
        }
    }

    private void reserveStockForItems(List<OrderItemRequestDTO> items) {
        items.forEach(item -> {
            log.info("Reserving {} units of SKU: {}", item.quantity(), item.sku());
            inventoryService.reserveStock(item.sku(), item.quantity());
        });
    }

    private Order buildOrder(OrderRequestDTO request, Map<String, BigDecimal> priceMap) {
        List<OrderItem> orderItems = buildOrderItems(request.items(), priceMap);

        return Order.builder()
                .customerName(request.customerName())
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>(orderItems))
                .build();
    }

    private List<OrderItem> buildOrderItems(List<OrderItemRequestDTO> items, Map<String, BigDecimal> priceMap) {
        return items.stream()
                .map(item -> toOrderItem(item, priceMap.get(item.sku())))
                .toList();
    }

    private OrderItem toOrderItem(OrderItemRequestDTO item, BigDecimal price) {
        return OrderItem.builder()
                .sku(item.sku())
                .quantity(item.quantity())
                .priceAtTimeOfOrder(price)
                .build();
    }

}
