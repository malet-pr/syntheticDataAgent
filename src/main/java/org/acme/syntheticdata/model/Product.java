package org.acme.syntheticdata.model;

import org.acme.syntheticdata.model.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Double price;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InventoryStatus inventoryStatus;
    private Integer rating;
    @Column(name = "active", length = 1)
    private Character active = 'Y';
}
