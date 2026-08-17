package org.acme.syntheticdata.model;

import org.acme.syntheticdata.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "customer_order")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String invoice;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @ManyToOne
    @JoinColumn(name = "representative_id")
    private Representative representative;
    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL,
                orphanRemoval = true, fetch =  FetchType.EAGER)
    private List<OrderLine> orderLines;
    private LocalDateTime orderDate;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status;
    @Column(name = "active", length = 1)
    private Character active = 'Y';
}
