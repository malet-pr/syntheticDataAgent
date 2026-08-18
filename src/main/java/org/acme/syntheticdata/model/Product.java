package org.acme.syntheticdata.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.acme.syntheticdata.model.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

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
    private BigDecimal price;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @JsonPropertyDescription("Valid Inventory_Status: INSTOCK, LOWSTOCK, OUTOFSTOCK. Do NOT use other statuses.")
    private InventoryStatus inventoryStatus;
    private Integer rating;
    @Column(name = "active", length = 1)
    private Character active = 'Y';
}
