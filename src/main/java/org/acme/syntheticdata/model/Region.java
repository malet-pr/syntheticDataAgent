package org.acme.syntheticdata.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "region")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String code;
    private String name;
    @Column(name = "active", length = 1)
    private Character active = 'Y';
}

