package org.acme.syntheticdata.config;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;


import com.google.genai.types.GenerateContentConfig;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.ChatOptions;


@Component
public class GoogleGenAiChatModel implements ChatModel {

    private final Client client;
    @Value("${spring.ai.vertex.ai.gemini.model}")
    private String modelName;

    public GoogleGenAiChatModel(Client client) {
        this.client = client;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        // 1. Extract System Message natively
        String systemInstruction = prompt.getInstructions().stream()
                .filter(msg -> msg.getMessageType() == MessageType.SYSTEM)
                .map(org.springframework.ai.content.Content::getText)
                .collect(Collectors.joining("\n"));

        // 2. Extract User / Conversation Messages
        String userContent = prompt.getInstructions().stream()
                .filter(msg -> msg.getMessageType() != MessageType.SYSTEM)
                .map(org.springframework.ai.content.Content::getText)
                .collect(Collectors.joining("\n"));

        // 3. Configure Gemini Native System Instructions
        GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();
        if (!systemInstruction.isBlank()) {
            configBuilder.systemInstruction(Content.fromJson(systemInstruction));
        }

        // 4. Execute API Call
        GenerateContentResponse response = client.models.generateContent(
                modelName,
                userContent,
                configBuilder.build()
        );

        String textResponse = response.text() != null ? response.text() : "";
        return new ChatResponse(List.of(new Generation(new AssistantMessage(textResponse))));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return null;
    }
}