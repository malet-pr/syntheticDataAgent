package org.acme.syntheticdata;

import org.acme.syntheticdata.tool.DataSeederTool;
import org.acme.syntheticdata.tool.DatabaseInspectorTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.stream.Stream;

@Component
public class AgentRunner implements CommandLineRunner {

    private final ChatModel chatModel;
    private final DatabaseInspectorTool databaseInspectorTool;
    private final DataSeederTool dataSeederTool;

    // Control execution from application.yml (Default: BOTH)
    @Value("${agent.mode:BOTH}")
    private String agentMode;

    public AgentRunner(ChatModel chatModel,
                       DatabaseInspectorTool databaseInspectorTool,
                       DataSeederTool dataSeederTool) {
        this.chatModel = chatModel;
        this.databaseInspectorTool = databaseInspectorTool;
        this.dataSeederTool = dataSeederTool;
    }

    @Override
    public void run(String... args) throws Exception {
        String mode = agentMode.toUpperCase();
        System.out.println("\n==================================================");
        System.out.println("  SYNTHETIC DATA AGENT — MODE: " + mode);
        System.out.println("==================================================");

        switch (mode) {
            case "INSPECT" -> inspectSchema();
            case "SEED" -> seedData();
            case "BOTH" -> {
                System.out.println("\n--- STEP 1: Pre-Seeding Inspection ---");
                inspectSchema();

                System.out.println("\n--- STEP 2: Autonomous Seeding ---");
                seedData();

                System.out.println("\n--- STEP 3: Post-Seeding Verification ---");
                inspectSchema();
            }
            default -> System.err.println("Unknown agent.mode '" + mode + "'. Use INSPECT, SEED, or BOTH.");
        }
    }

    private void inspectSchema() {
        ToolCallback[] tools = ToolCallbacks.from(databaseInspectorTool);

        ToolCallingChatOptions options = ToolCallingChatOptions.builder()
                .toolCallbacks(tools)
                .build();

        String directive = """
        You are an autonomous database inspection agent. 
        Perform all necessary tool calls immediately without asking for confirmation:
        1. Retrieve the list of all table names.
        2. For EVERY table found, call tools to get its structure and current row count.
        3. Present a complete final summary.
        Do not ask questions or stop halfway. Execute all tool calls now.
        """;

        Prompt prompt = new Prompt(directive, options);

        String response = chatModel.call(prompt).getResult().getOutput().getText();
        System.out.println(response);
    }

    private void seedData() {
        ToolCallback[] inspectorTools = ToolCallbacks.from(databaseInspectorTool);
        ToolCallback[] seederTools = ToolCallbacks.from(dataSeederTool);

        ToolCallback[] allTools = Stream.concat(Arrays.stream(inspectorTools), Arrays.stream(seederTools))
                .toArray(ToolCallback[]::new);

        ToolCallingChatOptions options = ToolCallingChatOptions.builder()
                .toolCallbacks(allTools)
                .build();

        String promptText = """
        Inspect the database schema, check existing rows, and seed 2 new customers with 1 order and 2 orderlines each.
        
        STRICT DATA INTEGRITY RULES:
        1. ENUM CASING: Status columns (e.g., invoice/order status) MUST strictly use uppercase enum values: 'PENDING', 'PAID', 'CANCELLED'. Never use Titlecase or lowercase.
        2. TIMESTAMPS: Always generate full realistic timestamps with explicit hours, minutes, and seconds (e.g., '2026-08-15 14:32:05'), never hardcode '00:00:00.000000'.
        3. QUOTING: Always double-quote reserved PostgreSQL table names like "order" in SQL statements.
        """;

        Prompt prompt = new Prompt(promptText, options);
        String response = chatModel.call(prompt).getResult().getOutput().getText();
        System.out.println(response);
    }
}