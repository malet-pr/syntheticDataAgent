package org.acme.syntheticdata;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.acme.syntheticdata.tool.DatabaseExecutionTool;
import org.acme.syntheticdata.tool.DatabaseInspectorTool;
import org.acme.syntheticdata.tool.DatabaseValidationTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class AgentRunner implements CommandLineRunner {

    @Value("${spring.ai.vertex.ai.gemini.project-id:fiery-glass-428422-j6}")
    private String projectId;

    @Value("${spring.ai.vertex.ai.gemini.location:global}")
    private String location;

    @Value("${spring.ai.vertex.ai.gemini.model:gemini-3.5-flash-lite}")
    private String model;

    @Autowired
    private DatabaseInspectorTool databaseInspectorTool;

    @Autowired
    private DatabaseExecutionTool databaseExecutionTool;

    @Autowired
    private DatabaseValidationTool databaseValidationTool;

    private List<Method> scanToolMethods(Class<?>... toolClasses) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> clazz : toolClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                boolean isTool = Arrays.stream(method.getAnnotations())
                        .anyMatch(a -> a.annotationType().getSimpleName().equals("Tool"));
                // Google SDK requires public static methods
                if (isTool && Modifier.isStatic(method.getModifiers())) {
                    methods.add(method);
                    log.info("[TOOL REGISTERED] Registered static method: {}.{}()",
                            clazz.getSimpleName(), method.getName());
                }
            }
        }
        if (methods.isEmpty()) {
            throw new IllegalStateException("No public static @Tool methods found in provided classes!");
        }
        return methods;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("\n--- RUNNING SYNTHETIC DATA AGENT WITH AUTOMATIC FUNCTION CALLING ---");

        List<Method> toolMethods = scanToolMethods(
                DatabaseInspectorTool.class,
                DatabaseExecutionTool.class,
                DatabaseValidationTool.class
        );

        AutomaticFunctionCallingConfig autoConfig = AutomaticFunctionCallingConfig.builder()
                .maximumRemoteCalls(25)
                .build();

        FunctionCallingConfig functionCallingConfig =
                FunctionCallingConfig.builder()
                        .mode(FunctionCallingConfigMode.Known.ANY)
                        .build();

        ToolConfig toolConfig =
                ToolConfig.builder()
                        .functionCallingConfig(functionCallingConfig)
                        .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .automaticFunctionCalling(
                        AutomaticFunctionCallingConfig.builder().maximumRemoteCalls(25).build()
                )
                .toolConfig(toolConfig)
                .tools(List.of(
                        Tool.builder().functions(toolMethods.toArray(new Method[0])).build()
                ))
                .build();

        Client client = Client.builder()
                .project(projectId)
                .location(location)
                .vertexAI(true)
                .build();

        Chat chat = client.chats.create(model, config);

        // Step 1: Base Reference Entities
        String step1 = """
                You are an autonomous database seeding agent.
                
                STRICT DATA GENERATION CONSTRAINTS:
                1. ZERO-NULL POLICY: Every single column in every row MUST be populated with an explicit, realistic value. NEVER omit columns or pass NULL, even if the database permits nulls (IS_NULLABLE = 'YES').
                2. TIMESTAMP INTEGRITY:
                   - For every timestamp/date field (e.g. 'created_at', 'order_date', 'updated_at'), generate valid ISO-8601 strings (e.g., '2026-08-17 14:30:00').
                   - Use checkTimestampFormatting(tableName, columnName) or validate your formatted strings before executing batch inserts.
                3. COMPLETE COLUMN MATCH:
                   - Inspect describeTable() for the target table.
                   - Every column listed in describeTable() MUST appear in your INSERT column list with a valid value.
                
                DATA REALISM CONSTRAINTS:
                - FORBIDDEN: Do NOT use placeholder patterns, codes, or foreign key syntax as names (e.g., NEVER generate 'C7-1', 'Mgr R1-1').
                - All descriptions and names MUST be realistic, fully spelled-out consumer products or real-world names.
                
                INSERT RULE:
                - Do NOT include primary key 'id' columns in your INSERT statements. Let the database auto-generate 'id' values automatically via DEFAULT/IDENTITY.
                
                EXECUTION SEQUENCE:
                1. Call describeTable() for target entities: 'region', 'manager', 'representative', 'product_category', 'product'.
                2. Generate synthetic data following constraints:
                   - 4 regions
                   - 7 product_categories
                   - 8 managers
                   - 25 representatives
                   - 25 products
                3. Persist data by calling executeSqlMutations() with a JSON array/list of valid INSERT statements in dependency order.
                4. Call runValidationQuery() to check total row counts.
                
                Execute these steps now using your provided tools.
                """;

        executeStep(chat, "Step 1 (Base Entities)", step1);

        // Step 2: Customer Entities
        String step2 = """
                You are an autonomous database seeding agent.
                
                STRICT DATA GENERATION CONSTRAINTS:
                1. ZERO-NULL POLICY: Every single column in every row MUST be populated with an explicit, realistic value. NEVER omit columns or pass NULL, even if the database permits nulls (IS_NULLABLE = 'YES').
                2. TIMESTAMP INTEGRITY:
                   - For every timestamp/date field (e.g. 'created_at', 'order_date', 'updated_at'), generate valid ISO-8601 strings (e.g., '2026-08-17 14:30:00').
                   - Use checkTimestampFormatting(tableName, columnName) or validate your formatted strings before executing batch inserts.
                3. COMPLETE COLUMN MATCH:
                   - Inspect describeTable() for the target table.
                   - Every column listed in describeTable() MUST appear in your INSERT column list with a valid value.
                
                DATA REALISM CONSTRAINTS:
                - FORBIDDEN: Do NOT use placeholder patterns, codes, or foreign key syntax as names (e.g., NEVER generate 'C7-1', 'Mgr R1-1').
                - All names MUST be realistic, fully spelled-out real-world names.
                
                INSERT RULE:
                - Do NOT include primary key 'id' columns in your INSERT statements. Let the database auto-generate 'id' values automatically via DEFAULT/IDENTITY.
                
                EXECUTION SEQUENCE:
                1. Call describeTable() for target entity: 'customer'.
                2. Query existing primary keys from parent tables ('region' and 'representative') using runValidationQuery().
                3. Generate 50 synthetic customer records with realistic names. Assign regions randomly and unevenly across all regions created.
                4. Persist data by calling executeSqlMutations() with standard INSERT statements.
                5. Call runValidationQuery() to verify customer row count.
                
                Execute these steps now using your provided tools.
                """;

        executeStep(chat, "Step 2 (Customers)", step2);

        // Step 3: Transactional Entities
        String step3 = """
                You are an autonomous database seeding agent.
                
                STRICT DATA GENERATION CONSTRAINTS:
                1. ZERO-NULL POLICY: Every single column in every row MUST be populated with an explicit, realistic value. NEVER omit columns or pass NULL, even if the database permits nulls (IS_NULLABLE = 'YES').
                2. TIMESTAMP INTEGRITY:
                   - For every timestamp/date field (e.g. 'created_at', 'order_date', 'updated_at'), generate valid ISO-8601 strings (e.g., '2026-08-17 14:30:00').
                   - Use checkTimestampFormatting(tableName, columnName) or validate your formatted strings before executing batch inserts.
                3. COMPLETE COLUMN MATCH:
                   - Inspect describeTable() for the target table.
                   - Every column listed in describeTable() MUST appear in your INSERT column list with a valid value.
                
                INSERT RULE:
                - Do NOT include primary key 'id' columns in your INSERT statements. Let the database auto-generate 'id' values automatically via DEFAULT/IDENTITY.
                
                EXECUTION SEQUENCE:
                1. Call describeTable() for target entities: 'customer_order' and 'orderline'.
                2. Query valid existing primary keys from parent tables ('customer' and 'product') using runValidationQuery().
                3. Generate synthetic transactional data:
                   - 50 customer_orders distributed unevenly among customers, with a median of 2 orders by customer.
                   - 1–5 orderlines per order matching product prices, distributed unevenly among orders with a median of 2.5 orders by order.
                4. Persist data by calling executeSqlMutations() ('customer_order' first, then 'orderline').
                5. Call runValidationQuery() to verify total row counts.
                
                Execute these steps now using your provided tools.
                """;

        executeStep(chat, "Step 3 (Orders Batch 1)", step3);
        executeStep(chat, "Step 3 (Orders Batch 2)", step3);
        executeStep(chat, "Step 3 (Orders Batch 3)", step3);
        executeStep(chat, "Step 3 (Orders Batch 4)", step3);
    }

    private void executeStep(Chat chat, String stepName, String prompt) {
        log.info("\n\n--- EXECUTING: {} ---", stepName);
        GenerateContentResponse response = chat.sendMessage(prompt);
        if (response.text() != null && !response.text().isBlank()) {
            log.info("Response: {}", response.text());
        } else {
            log.warn("No text response received for {}", stepName);
        }
        if (response.automaticFunctionCallingHistory().isPresent()) {
            log.info("\n--- Tool Execution History for {} ---", stepName);
            response.automaticFunctionCallingHistory().get().forEach(
                    hist -> log.info("Tool Call/Response: {}", hist.toString())
            );
        }
    }
}