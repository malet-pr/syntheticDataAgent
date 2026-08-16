package org.acme.syntheticdata.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseInspectorTool {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInspectorTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "Returns a list of all table names in the current database schema")
    public Map<String, Object> listTables() {
        try {
            String sql = """
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = 'public' 
                  AND table_type = 'BASE TABLE'
                """;
            List<String> tables = jdbcTemplate.queryForList(sql, String.class);
            return Map.of("status", "SUCCESS", "tables", tables);
        } catch (Exception e) {
            return Map.of("status", "ERROR", "message", e.getMessage());
        }
    }

    @Tool(description = "Returns table schema details including column names, data types, and nullability for a given table")
    public Map<String, Object> describeTable(String tableName) {
        try {
            String sql = """
                SELECT column_name, data_type, is_nullable 
                FROM information_schema.columns 
                WHERE table_name = ? AND table_schema = 'public'
                ORDER BY ordinal_position
                """;
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName);
            return Map.of("status", "SUCCESS", "tableName", tableName, "columns", columns);
        } catch (Exception e) {
            return Map.of("status", "ERROR", "tableName", tableName, "message", e.getMessage());
        }
    }

    @Tool(description = "Returns the total record count for a specific table to check existing data density")
    public Map<String, Object> getTableRowCount(String tableName) {
        try {
            // Strip non-alphanumeric chars and wrap in double quotes for PostgreSQL reserved words like "order"
            String sanitized = tableName.replaceAll("[^a-zA-Z0-9_]", "");
            String sql = "SELECT COUNT(*) FROM \"" + sanitized + "\"";

            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return Map.of("status", "SUCCESS", "tableName", tableName, "rowCount", count != null ? count : 0L);
        } catch (Exception e) {
            return Map.of("status", "ERROR", "tableName", tableName, "message", e.getMessage());
        }
    }
}