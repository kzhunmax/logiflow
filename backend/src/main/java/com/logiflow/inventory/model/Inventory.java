package com.logiflow.inventory.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "inventory")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Inventory {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private Integer quantity;
    private Integer reserved;
    private LocalDateTime lastUpdated;

    @Version
    private Long version;
}
