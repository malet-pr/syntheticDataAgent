package org.acme.syntheticdata.service;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.acme.syntheticdata.dto.SeedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.acme.syntheticdata.service.StepExecutor.executeBatchedStep;


@Service
@Slf4j
public class DatabaseAgentService {

    @Value("${spring.ai.max_batch_size}")
    int maxBatchSize;

    @Autowired
    private AgentBaseService base;

    public String runFullAgent(SeedRequest req) throws Exception {
        log.info("\n\n\n--- RUNNING FULL SYNTHETIC DATA AGENT WITH AUTOMATIC FUNCTION CALLING ---");

        Client client = Client.builder()
                .project(base.projectId)
                .location(base.location)
                .vertexAI(true)
                .build();

        Chat chat = client.chats.create(base.model, base.configClient());

        StringBuilder sb = new StringBuilder();

        // Logistic entities
        String step1 = Prompts.step1(req);
        if(step1 != null) {
            String resp = executeStep(chat, "Step 1 (Logistic)", step1);
            sb.append(resp).append("\n\n\n");
        }

        // Products Entities
        String step2 = Prompts.step2(req);
        if(step2 != null) {
            String resp = executeStep(chat, "Step 2 (Products)", step2);
            sb.append(resp).append("\n\n\n");
        }

        // Customer Entity
        String customerResults = executeBatchedStep(
                chat,
                "Step 3 (Customers)",
                req.customers(),
                maxBatchSize,
                Prompts::step3,
                (label, prompt) -> executeStep(chat, label, prompt)
        );
        sb.append(customerResults);

        // Customer_order Entity
        String orderResults = executeBatchedStep(
                chat,
                "Step 4 (Customer_orders)",
                req.customer_orders(),
                maxBatchSize,
                Prompts::step4,
                (label, prompt) -> executeStep(chat, label, prompt)
        );
        sb.append(orderResults);

        // Orderline Entity
        String orderlineResults = executeBatchedStep(
                chat,
                "Step 5 (Orderlines)",
                req.orderlines(),
                maxBatchSize,
                Prompts::step5,
                (label, prompt) -> executeStep(chat, label, prompt)
        );
        sb.append(orderlineResults);

        //log.info(sb.toString());
        return sb.toString();
    }

    private String executeStep(Chat chat, String stepName, String prompt) {
        if(prompt == null) {return "Empty prompt";}
        log.info("\n\n--- EXECUTING: {} ---", stepName);
        GenerateContentResponse response = chat.sendMessage(prompt);
        if (response.text() != null && !response.text().isBlank()) {
            log.debug("Response: {}", response.text());
        } else {
            log.warn("No text response received for {}", stepName);
        }
        if (response.automaticFunctionCallingHistory().isPresent()) {
            log.debug("\n\n--- Tool Execution History for {} ---", stepName);
            response.automaticFunctionCallingHistory().get().forEach(
                    hist -> log.debug("Tool Call/Response: {}", hist.toString())
            );
        }
        return response.text();
    }

}
