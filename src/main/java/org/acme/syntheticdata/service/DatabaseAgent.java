package org.acme.syntheticdata.service;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.acme.syntheticdata.dto.SeedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DatabaseAgent {

    @Autowired
    private AgentBase base;

    public void runFullAgent(SeedRequest req) throws Exception {
        log.info("\n\n\n--- RUNNING FULL SYNTHETIC DATA AGENT WITH AUTOMATIC FUNCTION CALLING ---");

        Client client = Client.builder()
                .project(base.projectId)
                .location(base.location)
                .vertexAI(true)
                .build();

        Chat chat = client.chats.create(base.model, base.configClient());

        // Structural entities
        String step1 = Prompts.step1(req);
        executeStep(chat, "Step 1 (Base Entities)", step1);

        // Customer Entity
        String step2 = Prompts.step2(req);
        executeStep(chat, "Step 2 (Customers)", step2);

        // Customer_order Entity
        String step3 = Prompts.step3(req);
        executeStep(chat, "Step 3 (Orders)", step3);

        // Orderline Entity
        String step4 = Prompts.step4(req);
        executeStep(chat, "Step 4 (Orderlines)", step4);
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
