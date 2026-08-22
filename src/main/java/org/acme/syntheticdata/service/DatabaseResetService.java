package org.acme.syntheticdata.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseResetService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseResetService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> resetTables() {
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

    public Map<String, Object> resetSequences() {
        try {
            String resetSequencesSql = """
                DO $$
                    DECLARE
                        r RECORD;
                    BEGIN
                        FOR r IN (SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public')
                        LOOP
                            EXECUTE 'ALTER SEQUENCE ' || quote_ident(r.sequence_name) || ' RESTART WITH 1';
                        END LOOP;
                    END $$;
            """;
            jdbcTemplate.execute(resetSequencesSql);
            return Map.of("status", "SUCCESS", "message", "Sequences reset.");
        } catch (Exception e) {
            return Map.of("status", "ERROR", "message", e.getMessage());
        }
    }

    public Map<String, Object> resetAllTables() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String,Object> map1 = this.resetTables();
            Map<String,Object> map2 = this.resetSequences();
            result.putAll(map1);
            result.putAll(map2);
            return result;
        } catch (Exception e) {
            return Map.of("status", "ERROR", "message", e.getMessage());
        }
    }
}