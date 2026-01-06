package com.logiflow.catalog.mapper;

import com.logiflow.catalog.dto.ProductRequestDTO;
import com.logiflow.catalog.dto.ProductResponseDTO;
import com.logiflow.catalog.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDTO dto) {
        if (dto == null) return null;
        return Product.builder()
                .name(dto.name())
                .sku(dto.sku())
                .price(dto.price())
                .attributes(dto.attributes())
                .active(true)
                .build();
    }

    public ProductResponseDTO toDto(Product product) {
        if (product == null) return null;
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

