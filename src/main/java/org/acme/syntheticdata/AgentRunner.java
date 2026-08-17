package org.acme.syntheticdata;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Component
public class AgentRunner implements CommandLineRunner {

    @Value("${spring.ai.vertex.ai.gemini.project-id:fiery-glass-428422-j6}")
    private String projectId;

    @Value("${spring.ai.vertex.ai.gemini.location:global}")
    private String location;

    @Value("${spring.ai.vertex.ai.gemini.model:gemini-3.5-flash-lite}")
    private String model;

    // --- Autonomous Database Tools ---

    public static String resetAllTables() {
        System.out.println("\n[TOOL EXECUTION] resetAllTables() invoked: Clearing target tables...");
        // Implement your JDBC/JPA database reset logic here
        return "SUCCESS: All tables have been truncated and reset.";
    }

    public static String describeTable(String tableName) {
        System.out.println("\n[TOOL EXECUTION] describeTable() invoked for: " + tableName);
        // Implement database schema metadata inspection here
        return "SCHEMA for " + tableName + ": id (BIGINT, PK), code (VARCHAR, UNIQUE), description (TEXT), created_at (TIMESTAMP)";
    }

    public static String runValidationQuery(String query) {
        System.out.println("\n[TOOL EXECUTION] runValidationQuery() invoked with: " + query);
        // Implement database row-count and foreign key validation here
        return "VALIDATION SUCCESS: Data integrity verified, zero constraint violations.";
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n--- RUNNING SYNTHETIC DATA AGENT WITH AUTOMATIC FUNCTION CALLING ---");

        Client client = Client.builder()
                .project(projectId)
                .location(location)
                .vertexAI(true)
                .build();

        // Bind Java methods for Automatic Function Calling (AFC)
        Method resetMethod = AgentRunner.class.getMethod("resetAllTables");
        Method describeMethod = AgentRunner.class.getMethod("describeTable", String.class);
        Method validateMethod = AgentRunner.class.getMethod("runValidationQuery", String.class);

        GenerateContentConfig config = GenerateContentConfig.builder()
                .tools(Tool.builder().functions(resetMethod, describeMethod, validateMethod))
                .build();

        String promptText = """
                You are an autonomous database seeding agent.
                
                EXECUTION SEQUENCE:
                1. Call resetAllTables().
                2. Call describeTable() for target entity 'orders'.
                3. Conclude by calling runValidationQuery() with a check statement.
                
                Execute these steps sequentially now using your provided tools.
                """;

        GenerateContentResponse response = client.models.generateContent(
                model,
                promptText,
                config
        );

        System.out.println("\n--- Final Agent Response ---");
        System.out.println(response.text() != null ? response.text() : "No text returned.");

        // Print automatic execution history logs
        if (response.automaticFunctionCallingHistory().isPresent()) {
            System.out.println("\n--- Autonomous Execution History ---");
            response.automaticFunctionCallingHistory().get().forEach(step ->
                    System.out.println(step)
            );
        }
    }
}