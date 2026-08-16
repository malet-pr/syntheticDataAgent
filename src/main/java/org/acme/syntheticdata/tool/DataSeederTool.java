package org.acme.syntheticdata.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class DataSeederTool {

    private final JdbcTemplate jdbcTemplate;

    public DataSeederTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "Executes a list of SQL INSERT statements sequentially to generate synthetic records.")
    public Map<String, Object> executeSqlInserts(List<String> sqlStatements) {
        int rowsInserted = 0;

        for (String sql : sqlStatements) {
            String trimmedSql = sql.trim();

            if (!trimmedSql.toLowerCase().startsWith("insert")) {
                return Map.of(
                        "status", "ERROR",
                        "message", "Rejected non-INSERT statement: " + trimmedSql
                );
            }

            try {
                rowsInserted += jdbcTemplate.update(trimmedSql);
            } catch (Exception e) {
                // Catching SQL exceptions prevents unquoted Exception text from breaking Jackson serialization
                return Map.of(
                        "status", "ERROR",
                        "failedStatement", trimmedSql,
                        "message", e.getMessage() != null ? e.getMessage() : "Database error during execution"
                );
            }
        }

        return Map.of(
                "status", "SUCCESS",
                "rowsInserted", rowsInserted
        );
    }
}