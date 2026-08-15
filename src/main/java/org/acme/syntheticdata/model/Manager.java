package org.acme.syntheticdata.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "manager")
public class Manager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String code;
    private String name;
    @ManyToOne( cascade = { CascadeType.ALL } )
    @JoinColumn(name = "region_id")
    private Region region;
    @Column(name = "active", length = 1)
    private Character active = 'Y';
}
