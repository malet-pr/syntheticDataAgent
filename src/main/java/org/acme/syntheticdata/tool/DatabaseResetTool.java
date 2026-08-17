package org.acme.syntheticdata.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseResetTool {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseResetTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(name = "resetAllTables", description = "DANGER: ALWAYS use this dedicated tool when asked to reset, clear, wipe, or truncate all tables. Do NOT execute raw DELETE or TRUNCATE SQL statements.")
    public Map<String, Object> resetAllTables() {
        try {
            String fetchTablesSql = """
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = 'public' 
                  AND table_type = 'BASE TABLE'
                  AND table_name != 'flyway_schema_history'
                """;

            List<String> tables = jdbcTemplate.queryForList(fetchTablesSql, String.class);

            if (tables.isEmpty()) {
                return Map.of("status", "SUCCESS", "message", "No user tables found to truncate.");
            }

            // Combine into a single TRUNCATE statement with CASCADE for PostgreSQL
            StringBuilder truncateSql = new StringBuilder("TRUNCATE TABLE ");
            for (int i = 0; i < tables.size(); i++) {
                truncateSql.append("\"").append(tables.get(i)).append("\"");
                if (i < tables.size() - 1) {
                    truncateSql.append(", ");
                }
            }
            truncateSql.append(" CASCADE;");

            jdbcTemplate.execute(truncateSql.toString());

            return Map.of("status", "SUCCESS", "truncatedTables", tables);
        } catch (Exception e) {
            return Map.of("status", "ERROR", "message", e.getMessage());
        }
    }
}