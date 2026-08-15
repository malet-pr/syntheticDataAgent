package org.acme.syntheticdata.model;

import org.acme.syntheticdata.model.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String code;
    private String name;
    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;
    private LocalDateTime joinDate;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CustomerStatus status;
    private Boolean verified;
    @Column(name = "active", length = 1)
    private Character active = 'Y';
}

