package org.acme.syntheticdata.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;


@Component
public class DatabaseValidationTool {

    private static JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        DatabaseValidationTool.jdbcTemplate = jdbcTemplate;
    }

    @Tool(name = "runValidationQuery" ,description = "Executes a SELECT query to audit data integrity rules (e.g. check for missing values or constraint violations). DO NOT pass INSERT/UPDATE here.")
    public static Map<String, Object> runValidationQuery(String sqlQuery, String ruleDescription) {
        try {
            if (sqlQuery.trim().toUpperCase().startsWith("INSERT") ||
                    sqlQuery.trim().toUpperCase().startsWith("UPDATE")) {
                return Map.of("status", "ERROR", "message", "Use executeSql tool for database mutations, not runValidationQuery.");
            }

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

    @Tool(name = "checkTimestampFormatting", description = "Checks if any records in a table contain zeroed-out or default timestamp values (e.g., '00:00:00')")
    public static Map<String, Object> checkTimestampFormatting(String tableName, String columnName) {
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
            return Map.of("status", "ERROR", "message", "Validation failed: " + e.getMessage());
        }
    }
}