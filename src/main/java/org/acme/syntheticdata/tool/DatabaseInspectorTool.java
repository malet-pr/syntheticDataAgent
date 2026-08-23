package org.acme.syntheticdata.tool;

import org.acme.syntheticdata.dto.SequenceInfo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.acme.syntheticdata.tool.ToolResultSanitizer.sanitizeMap;

@Component
public class DatabaseInspectorTool {
    
    private static JdbcTemplate jdbcTemplate;
    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        DatabaseInspectorTool.jdbcTemplate = jdbcTemplate;
    }


    @Tool(name = "listTables",description = "Returns a list of all table names in the current database schema")
    public static Map<String, Object> listTables() {
        try {
            String sql = """
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = 'public' 
                  AND table_type = 'BASE TABLE'
                """;
            List<String> tables = jdbcTemplate.queryForList(sql, String.class);
            return sanitizeMap(Map.of("status", "SUCCESS", "tables", tables));
        } catch (Exception e) {
            return Map.of("status", "ERROR", "message", e.getMessage());
        }
    }

    @Tool(name = "describeTable",description = "Returns table schema details including column names, data types, and nullability for a given table")
    public static Map<String, Object> describeTable(String tableName) {
        try {
            String sql = """
                SELECT column_name, data_type, is_nullable 
                FROM information_schema.columns 
                WHERE table_name = ? AND table_schema = 'public'
                ORDER BY ordinal_position
                """;
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName);
            return sanitizeMap(Map.of("status", "SUCCESS", "tableName", tableName, "columns", columns));
        } catch (Exception e) {
            return Map.of("status", "ERROR", "tableName", tableName, "message", e.getMessage());
        }
    }

/*
    @Tool(name = "findSequences",description = "Returns the sequence name and its maximum value for primary keys")
    public static Map<String,Object> findSequences() {
        try {
            String sql = """
                    SELECT
                        t.relname AS table_name,
                        s.relname AS sequence_name,
                        seq.last_value AS sequence_number
                    FROM pg_class s
                    JOIN pg_depend d ON d.objid = s.oid
                    JOIN pg_class t ON d.refobjid = t.oid
                    JOIN pg_namespace n ON n.oid = s.relnamespace
                    JOIN pg_sequences seq ON seq.schemaname = n.nspname AND seq.sequencename = s.relname
                    WHERE s.relkind = 'S'
                      AND t.relname NOT LIKE 'flyway_%'
                      AND n.nspname = 'public'
                    ORDER BY t.relname
                """;
            List<SequenceInfo> sequences = jdbcTemplate.queryForList(sql,SequenceInfo.class);
            return sanitizeMap(Map.of("status", "SUCCESS","sequences", sequences));
        } catch (Exception e) {
            return Map.of("status", "ERROR", "sequences", Collections.EMPTY_LIST, "message", e.getMessage());
        }
    }
*/

    @Tool(name = "getTableRowCount", description = "Returns the total record count for a specific table to check existing data density")
    public static Map<String, Object> getTableRowCount(String tableName) {
        try {
            // Strip non-alphanumeric chars and wrap in double quotes for PostgreSQL reserved words like "order"
            String sanitized = tableName.replaceAll("[^a-zA-Z0-9_]", "");
            String sql = "SELECT COUNT(*) FROM \"" + sanitized + "\"";

            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return sanitizeMap(Map.of("status", "SUCCESS", "tableName", tableName, "rowCount", count != null ? count : 0L));
        } catch (Exception e) {
            return Map.of("status", "ERROR", "tableName", tableName, "message", e.getMessage());
        }
    }
}