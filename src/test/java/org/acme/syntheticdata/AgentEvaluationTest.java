package org.acme.syntheticdata;

import lombok.extern.slf4j.Slf4j;
import org.acme.syntheticdata.dto.SeedRequest;
import org.acme.syntheticdata.service.DatabaseAgentService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@Testcontainers
@SpringBootTest
@ActiveProfiles("eval")
@Slf4j
class AgentEvaluationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16")
                    .withDatabaseName("synthetic_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private  JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseAgentService databaseAgentService;

    private static boolean initialized = false;

    @BeforeEach
    void setup() throws Exception {
        if (!initialized) {
            SeedRequest request = new SeedRequest(2, 2, 2, 4, 5, 10, 20, 30);
            databaseAgentService.runFullAgent(request);
            initialized = true;
        }
    }

    @Test
    void generatedOrderLinesHaveValidAmounts() throws Exception {
        Integer violations = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM orderline ol
        JOIN product p ON p.id = ol.product_id
        WHERE ABS(ol.amount - p.price * ol.quantity) > 0.01
        """,
                Integer.class
        );
        assertEquals(0, violations);
    }

    @Test
    void orderStatusDistributionMatchesConfiguredRules() throws Exception {
        Map<String, Integer> counts = jdbcTemplate.query("""
        SELECT status, COUNT(*)
        FROM customer_order
        GROUP BY status
        """,
                rs -> {
                    Map<String, Integer> result = new HashMap<>();
                    while (rs.next()) {
                        result.put(
                                rs.getString("status"),
                                rs.getInt(2)
                        );
                    }
                    return result;
                }
        );
        assertEquals(14, counts.get("DELIVERED"));
        assertEquals(1, counts.get("PENDING"));
        assertEquals(3, counts.get("CANCELLED"));
        assertEquals(2, counts.get("RETURNED"));
    }

}
