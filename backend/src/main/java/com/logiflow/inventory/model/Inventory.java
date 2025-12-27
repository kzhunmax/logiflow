package com.logiflow.inventory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Inventory entity representing stock levels for a product")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique inventory record identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Schema(description = "Stock Keeping Unit - unique product identifier", example = "WM-001")
    private String sku;

    @Column(nullable = false)
    @Schema(description = "Total quantity in stock", example = "200")
    private Integer quantity;

    @Builder.Default
    @Column(nullable = false)
    @Schema(description = "Quantity reserved for pending orders", example = "50")
    private Integer reserved = 0;

    @Schema(description = "Timestamp of the last inventory update", example = "2025-12-27T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastUpdated;

    @Version
    @Schema(description = "Version number for optimistic locking", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long version;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
