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
    public List<String> listTables() {
        String sql = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
              AND table_type = 'BASE TABLE'
            """;
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Tool(description = "Returns table schema details including column names, data types, and nullability for a given table")
    public List<Map<String, Object>> describeTable(String tableName) {
        String sql = """
            SELECT column_name, data_type, is_nullable 
            FROM information_schema.columns 
            WHERE table_name = ? AND table_schema = 'public'
            ORDER BY ordinal_position
            """;
        return jdbcTemplate.queryForList(sql, tableName);
    }

    @Tool(description = "Returns the total record count for a specific table to check existing data density")
    public Long getTableRowCount(String tableName) {
        // Basic sanitization to prevent injection on internal table parameter
        String safeTableName = tableName.replaceAll("[^a-zA-Z0-9_]", "");
        String sql = "SELECT COUNT(*) FROM " + safeTableName;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
