package org.acme.syntheticdata.service;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.acme.syntheticdata.dto.SeedRequest;
import org.acme.syntheticdata.tool.DatabaseExecutionTool;
import org.acme.syntheticdata.tool.DatabaseInspectorTool;
import org.acme.syntheticdata.tool.DatabaseValidationTool;
import org.acme.syntheticdata.tool.EnumsFetchTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component
@Slf4j
public class FullDatabaseAgent {

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

    @Autowired
    private EnumsFetchTool enumsFetchTool;

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

    private String loadSkillFile(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) return "";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load skills file", e);
        }
    }

    private GenerateContentConfig configClient() {
        List<Method> toolMethods = scanToolMethods(
                DatabaseInspectorTool.class,
                DatabaseExecutionTool.class,
                DatabaseValidationTool.class,
                EnumsFetchTool.class
        );
        String systemSkills = loadSkillFile("skillsDraft.md");
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
        return GenerateContentConfig.builder()
                .systemInstruction(Content.builder()
                        .parts(List.of(Part.fromText(systemSkills)))
                        .build())
                .automaticFunctionCalling(autoConfig)
                .toolConfig(toolConfig)
                .tools(List.of(
                        Tool.builder().functions(toolMethods.toArray(new Method[0])).build()
                ))
                .build();
    }

    public void runFullAgent(SeedRequest req) throws Exception {
        log.info("\n\n\n--- RUNNING SYNTHETIC DATA AGENT WITH AUTOMATIC FUNCTION CALLING ---");

        Client client = Client.builder()
                .project(projectId)
                .location(location)
                .vertexAI(true)
                .build();

        Chat chat = client.chats.create(model, this.configClient());

        // Step 1: Base Reference Entities
        String step1 = """
                You are an autonomous database seeding agent.
                Use data-generation skill to generate:
                   - %d regions
                   - %d product_categories
                   - %d managers
                   - %d representatives
                   - %d products
                Use data-insertion skill to insert the sql generated.
                """.formatted(req.regions(),req.product_categories(),req.managers(),req.representatives(),req.products());
        executeStep(chat, "Step 1 (Base Entities)", step1);

        // Step 2: Customer Entities
        String step2 = """
                You are an autonomous database seeding agent.
                Use data-generation skill to generate %d customers.
                Follow these directives:
                   - Customers that are not verified cannot be active.
                   - Customers in any status can be inactives.
                   - Only NEW customers can be not verified.
                Use data-insertion skill to insert the sql generated.
                """.formatted(req.customers());
        executeStep(chat, "Step 2 (Customers)", step2);

        // Step 3: Transactional Entities
        for (int batch = 1; batch <= 4; batch++) {
            String step3Batch = """
            You are an autonomous database seeding agent.
            This is BATCH %d of 4.
            
            STEP 1:
                - Use data-generation skill to generate %d NEW customer_orders.
                - These MUST be additional unique orders for existing customers (do not re-insert or replace existing orders).
                - Use data-insertion skill to insert the generated SQL.
            STEP 2:
                - Use data-generation skill to generate %d NEW orderlines for the newly created orders in this batch.
                - For each orderline: amount = product_price * quantity.
                - No more than 30 percent of the orders can have only 1 orderline.
                - Use data-insertion skill to insert the generated SQL.
            """.formatted(batch, req.customer_orders(), req.orderlines());
            executeStep(chat, "Step 3 (Orders Batch " + batch + ")", step3Batch);
        }
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
            log.info("\n\n--- Tool Execution History for {} ---", stepName);
            response.automaticFunctionCallingHistory().get().forEach(
                    hist -> log.info("Tool Call/Response: {}", hist.toString())
            );
        }
    }

}
