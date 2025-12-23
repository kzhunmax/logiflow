package com.logiflow.inventory.model;

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
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Builder.Default
    @Column(nullable = false)
    private Integer reserved = 0;

    private LocalDateTime lastUpdated;

    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
