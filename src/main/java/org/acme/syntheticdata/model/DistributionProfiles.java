package org.acme.syntheticdata.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name="data_distribution_profile")
public class DistributionProfiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String tableName;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distribution_rules", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> distributionRules;
    private LocalDateTime updatedAt = LocalDateTime.now();
}
