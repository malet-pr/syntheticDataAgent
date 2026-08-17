package org.acme.syntheticdata.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseValidationTool {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseValidationTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "Executes a validation SQL query to verify data integrity rules (e.g. check casing or non-null constraints)")
    public Map<String, Object> runValidationQuery(String sqlQuery, String ruleDescription) {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sqlQuery);
            boolean isValid = result.isEmpty();

            return Map.of(
                    "status", "SUCCESS",
                    "ruleDescription", ruleDescription,
                    "passed", isValid,
                    "violationsCount", result.size(),
                    "sampleViolations", result.stream().limit(5).toList()
            );
        } catch (Exception e) {
            return Map.of("status", "ERROR", "ruleDescription", ruleDescription, "message", e.getMessage());
        }
    }

    @Tool(description = "Checks if any records in a table contain zeroed-out or default timestamp values (e.g., '00:00:00')")
    public Map<String, Object> checkTimestampFormatting(String tableName, String columnName) {
        try {
            String sanitizedTable = tableName.replaceAll("[^a-zA-Z0-9_]", "");
            String sanitizedColumn = columnName.replaceAll("[^a-zA-Z0-9_]", "");

            String sql = String.format(
                    "SELECT COUNT(*) FROM \"%s\" WHERE CAST(\"%s\" AS VARCHAR) LIKE '%%00:00:00%%'",
                    sanitizedTable, sanitizedColumn
            );

            Long invalidCount = jdbcTemplate.queryForObject(sql, Long.class);
            boolean passed = invalidCount != null && invalidCount == 0;

            return Map.of(
                    "status", "SUCCESS",
                    "tableName", tableName,
                    "columnName", columnName,
                    "passed", passed,
                    "zeroedTimestampCount", invalidCount != null ? invalidCount : 0
            );
        } catch (Exception e) {
            return Map.of("status", "ERROR", "message", e.getMessage());
        }
    }
}
