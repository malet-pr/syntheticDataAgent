package org.acme.syntheticdata.tool;

import org.springframework.stereotype.Component;

import java.sql.Array;
import java.sql.SQLException;
import java.util.*;

@Component
public class ToolResultSanitizer {

    public static Map<String, Object> sanitizeMap(Map<String, Object> map) {
        if (map == null) return Collections.emptyMap();
        Map<String, Object> cleanMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            cleanMap.put(entry.getKey(), sanitizeObject(entry.getValue()));
        }
        return cleanMap;
    }

    public static List<Object> sanitizeList(List<?> list) {
        if (list == null) return Collections.emptyList();
        List<Object> cleanList = new ArrayList<>();
        for (Object item : list) {
            cleanList.add(sanitizeObject(item));
        }
        return cleanList;
    }

    private static Object sanitizeObject(Object obj) {
        if (obj == null) return null;

        // Unwrap JDBC Array / PgArray
        if (obj instanceof Array sqlArray) {
            try {
                Object arrayBody = sqlArray.getArray();
                if (arrayBody instanceof Object[] javaArray) {
                    return sanitizeList(Arrays.asList(javaArray));
                }
                return sqlArray.toString();
            } catch (SQLException e) {
                return sqlArray.toString();
            }
        }

        // Recursively sanitize nested structures
        if (obj instanceof Map<?, ?> nestedMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typedMap = (Map<String, Object>) nestedMap;
            return sanitizeMap(typedMap);
        }

        if (obj instanceof List<?> nestedList) {
            return sanitizeList(nestedList);
        }

        // If it's a complex JDBC/Postgres object, convert to String representation
        String className = obj.getClass().getName();
        if (className.startsWith("org.postgresql.") || className.startsWith("java.sql.")) {
            return obj.toString();
        }

        return obj;
    }
}