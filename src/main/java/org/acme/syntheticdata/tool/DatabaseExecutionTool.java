package org.acme.syntheticdata.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseExecutionTool {

    private static JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        DatabaseExecutionTool.jdbcTemplate = jdbcTemplate;
    }

    @Tool(name = "executeSqlMutations", description = "Executes a ordered list of SQL mutation statements (INSERT, UPDATE, DELETE) sequentially inside a batch. Do not include semicolons at the end of statements.")
    public static Map<String, Object> executeSqlMutations(List<String> sqlStatements) {
        int totalRowsAffected = 0;
        int executedCount = 0;

        for (String sql : sqlStatements) {
            String trimmedSql = sql.trim();
            if (trimmedSql.endsWith(";")) {
                trimmedSql = trimmedSql.substring(0, trimmedSql.length() - 1).trim();
            }

            if (trimmedSql.isEmpty()) {
                continue;
            }

            // Basic validation guard against DDL or read-only queries if required
            String lower = trimmedSql.toLowerCase();
            if (!lower.startsWith("insert") && !lower.startsWith("update") && !lower.startsWith("delete")) {
                return Map.of(
                        "status", "ERROR",
                        "message", "Rejected non-mutation statement at index " + executedCount + ": " + trimmedSql
                );
            }

            try {
                totalRowsAffected += jdbcTemplate.update(trimmedSql);
                executedCount++;
            } catch (Exception e) {
                return Map.of(
                        "status", "ERROR",
                        "failedStatement", trimmedSql,
                        "failedIndex", executedCount,
                        "message", e.getMessage() != null ? e.getMessage() : "Database error during execution"
                );
            }
        }

        return Map.of(
                "status", "SUCCESS",
                "statementsExecuted", executedCount,
                "rowsAffected", totalRowsAffected
        );
    }
}

